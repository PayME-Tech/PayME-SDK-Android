1. Build and upload to maven:

./gradlew clean build install bintrayUpload -Ppublish=true --info      

2. Add other library if there is no library
----
implementation 'com.google.android.material:material:1.2.1'
implementation 'com.android.volley:volley:1.1.1'
implementation 'com.chaos.view:pinview:1.4.4'
implementation 'org.greenrobot:eventbus:3.0.0'
implementation 'com.airbnb.android:lottie:3.0.3'