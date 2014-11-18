#Requirements

- Interactive Spaces uses the Gradle build tool. There is no need to download
Gradle,it will be downloaded automatically by the build process.
- The Interactive Spaces Dependency git repo
- Clone https://github.com/interactivespaces/interactivespaces-dependencies.git
- The Android SDK
- Create a file in the root folder of the Interactive Spaces project called
gradle.properties.

This file should contain the following properties. Each of the properties
ending in .home say where you have a package installed. The examples
below give a fake place where each software package is installed, you will
change these to match where you installed the particular packages.

```
android.sdk.home = /home/you/software/android/android-sdk-linux_86
interactivespaces.dependencies.home = /home/you/software/repos/interactivespaces-dependencies
```

- The Android controller is built for a given minimum version of Android.
The following example shows building the Android controller for Ice Cream
Sandwich.

```
android.platform = android-16
```

- interactivespaces.dependencies.home should point at where you have installed
the git repo clone for the Interactive Spaces Dependencies repo.

- Install ROS on your computer. Install from www.ros.org. I usually install the
desktop full version, though there may be a smaller version that will work.

```
sudo apt-get install ros-indigo-desktop-full
```

- Place your interactivespaces repository on the ROS package path.

```
sudo mv interactivespaces /opt/ros/indigo/share/
```

- The Interactive Spaces documentation is built with the Python documentation 
system Sphinx. It uses Latex for building the PDF documentation. On Linux, make
sure you install texlive, textlive-latex-extra, and texlive-fonts-*. You also
need pygments.

```
sudo apt-get install texlive texlive-latex-extra texlive-fonts-* 
sudo pip install pygments
```

- You may also want to install pthread library:

```
sudo apt-get install libevent-pthreads-2.0-5
```

#Building

- Building installers:

If you want to build the installers, use the following command

```
./gradlew createInstallers
```

This will create installers for the master, controller, and workbench. The
installers will be found in

```
interactivespaces_build/*/build/distributions
```

where * can be master, controller, or workbench.

- Building an Image:

```
./gradlew -PimageHome=path createImage
```

where path is the root folder which will receive the image.

The image will contain a master, controller, and workbench.

- Updating a Dev instance from an Interactive Spaces Build:

Add the following to your gradle.properties file.

```
interactivespaces.dev.home=/home/you/interactivespaces
```

The value is where you have an instance of Interactive Spaces that you use for
developing and testing Interactive Spaces activities. This folder should
contain subfolders master, controller, workbench for each compoenent of an
Interactive Spaces development environment.

- To build Interactive Spaces and install updated files into your development
instance, use

```
./gradlew installDev
```
