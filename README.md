# comp433-class-work-04

## Add adb (Android Debug Bridge) to $PATH

```bash
# Add the following lines to .zshrc

# Add Android SDK platform-tools to PATH
command -v ~/Library/Android/sdk/platform-tools/adb > /dev/null
if [ $? -eq 0 ]; then
    export PATH=$PATH:~/Library/Android/sdk/platform-tools
    echo "Added Android SDK platform-tools to PATH."
fi
```

### Making sure that the emulator (and your application) has internet access

In `AndroidManfiest.xml`, make sure that you have the following line:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

In your emulator, open Google Chrome and make sure you can go to any website.

If that does not work, you'll need to use `adb` to troubleshoot further:

```bash
adb version
adb devices
adb shell ping -c 3 8.8.8.8
adb shell ping -c 3 google.com
```

My issue was that the ping to `google.com` was failing,. which points to a DNS
issue.

On macOS, go to System Settings --> Network --> Wi-Fi --> Details --> DNS.

Under DNS Servers, I had the following:

192.168.1.1
2001:1998:f00:2::1
2001:1998:f00:1::1

ChatGPT suggested adding the following:

`8.8.8.8`
`8.8.4.4`

The first three were there by default and went away once I added `8.8.8.8`.

Cold boot the emulator and try the ping commands again in the macOS terminal. To
cold boot, go to Tools --> Device Manager. Stop the emulator if it is running
and click the three vertical dots to enable to "Cold Boot Now" option.

Opening Google Chrome on the emulator should load a page successfully and these
commands should now work:

```bash
adb shell ping -c 3 8.8.8.8
adb shell ping -c 3 google.com
```

Now, communication with the Google Gemini API should be possible.
