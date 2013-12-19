package ds.xposed.mod.cleancallcard;

import android.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModDialer implements IXposedHookLoadPackage, IXposedHookInitPackageResources {

	public static final List<String> PACKAGE_NAMES = new ArrayList<>(Arrays.asList("com.google.android.dialer", "com.android.dialer", "com.android.incallui"));

	private static final String DIALER_PACKAGE = "com.android.dialer";
	private static final String CLASS_CALL_BUTTON_FRAGMENT = "com.android.incallui.CallButtonFragment";


	private static void log(String message) {
		XposedBridge.log("DS:KitKat ModDialer" + ": " + message);
	}


	@Override
	public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!ModDialer.PACKAGE_NAMES.contains(lpparam.packageName)) {
			return;
		}

		try {
			final Class inCallActivity = XposedHelpers.findClass("com.android.incallui.InCallActivity", lpparam.classLoader);
			final Class<?> classCallButtonFragment = XposedHelpers.findClass(CLASS_CALL_BUTTON_FRAGMENT, lpparam.classLoader);

			XposedHelpers.findAndHookMethod(inCallActivity, "initializeInCall", new XC_MethodHook() {
				protected void afterHookedMethod(XC_MethodHook.MethodHookParam paramMethodHookParam) throws Throwable {
					log("initializeInCall");
					View glowpad = ((Fragment) XposedHelpers.getObjectField(paramMethodHookParam.thisObject, "mAnswerFragment")).getView();
					glowpad.setBackgroundColor(0);
					FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) glowpad.getLayoutParams();
					p.height = dp(320, glowpad.getContext().getResources().getDisplayMetrics());
					p.bottomMargin = dp(-52, glowpad.getContext().getResources().getDisplayMetrics());
					//glowpad.setLayoutParams(p);
				}
			});

			XposedHelpers.findAndHookMethod(classCallButtonFragment, "setEnabled", boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					final View view = ((Fragment) param.thisObject).getView();
					if (view != null) {
						final boolean shouldHide = !(Boolean) param.args[0];
						view.setVisibility(shouldHide ? View.INVISIBLE : View.VISIBLE);
					}
				}
			});


		} catch (Throwable t) {
			XposedBridge.log(t);
		}
	}


	@Override
	public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
		if (!PACKAGE_NAMES.contains(resparam.packageName))
			return;

		log("handlePackage");
		try {
			resparam.res.setReplacement(DIALER_PACKAGE, "color", "incall_call_banner_background", 0);
			resparam.res.setReplacement(DIALER_PACKAGE, "color", "button_background", 0);
			resparam.res.setReplacement(DIALER_PACKAGE, "color", "incall_secondary_info_background", 0);

			log("Completed");
		} catch (Throwable localThrowable) {
			XposedBridge.log(localThrowable);
		}
	}


	private static int dp(int dp, DisplayMetrics m) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, m);
	}
}