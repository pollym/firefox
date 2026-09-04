####################################################################################################
# Catchall broadest possible keep rule for fenix
####################################################################################################

-keep class org.mozilla.fenix.** { *; }

####################################################################################################
# GeckoView built-ins
####################################################################################################

-keep class org.mozilla.geckoview.** { *; }

# Raptor now writes a *-config.yaml file to specify Gecko runtime settings (e.g. the profile dir). This
# file gets deserialized into a DebugConfig object, which is why we need to keep this class
# and its members.
-keep class org.mozilla.gecko.util.DebugConfig { *; }

####################################################################################################
# Remove debug logs from release builds
####################################################################################################
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

####################################################################################################
# Mozilla Application Services
####################################################################################################

-keep class mozilla.appservices.** { *; }

# Keep code generated from Glean Metrics
-keep class org.mozilla.fenix.GleanMetrics.** {  *; }

####################################################################################################
# Navigation argument types
####################################################################################################

# res/navigation/nav_graph.xml names these classes as app:argType strings, and AndroidX Navigation
# resolves them with Class.forName, so they must not be renamed. androidx.navigation's own consumer
# rules only cover NavArgs.fromBundle and Navigator subclasses, not argument types. Fenix's own
# argTypes are already covered by the catchall at the top of this file. CREATOR fields do not need
# keeping here; proguard-android-optimize.txt preserves them for all Parcelables.
-keep class mozilla.components.browser.state.state.content.PermissionHighlightsState
-keep class mozilla.components.concept.engine.permission.SitePermissions
-keep class mozilla.components.concept.engine.prompt.ShareData
-keep class mozilla.components.concept.engine.translate.ModelState
-keep class mozilla.components.concept.engine.webextension.InstallationMethod
-keep class mozilla.components.concept.storage.Address
-keep class mozilla.components.concept.storage.CreditCard
-keep class mozilla.components.feature.addons.Addon
