Apollo-CM
=========
Based on commit 4612f2b164de0f40d6916da2885a37d5d98214a2 from https://github.com/CyanogenMod/android_packages_apps_Apollo.git.
Added new functionally: Exclude files mask for excluding files or folders. 
It was needed for me after replace internal sdcard to external. 
So I get strange situation: Dublicates albums, artists, songs and other. 
It can't fixed by adding ".nomedia" to folder, because it need to exclude whole storage (for example external sdcard).

Android yacht player
