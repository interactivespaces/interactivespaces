
function colorize_output {
  if [ "$1" == "reset" ]; then
    tput setaf 7
  elif [ "$1" == "OK" ]; then
    tput setaf 2
  elif [ "$1" == "install" ]; then
    tput setaf 4
  else
    tput setaf 5
  fi
}

function dpkg_version {
  dpkg -l | egrep "^ii +$1 " | awk '{print $3}'
}

function version_status {
  ver=$1
  installed=$2

  if [ "$installed" ]; then
    # This is a simplistic comparison that will work for some version
    # strings... but not all. Should be re-written with something more
    # smart.
    if [[ "$ver" > "$installed" ]]; then
      status=BAD
    else
      status=OK
    fi
  else
    status=MISSING
  fi
  echo $status
}

# Arguments are: package, intended_version, status, installed_version
# installed_version may be empty if the package is not installed.
function report_status {
  colorize_output $3
  echo $3 $1 $2 $4
  colorize_output reset
}

function package {
  pkg=$1
  ver=$2

  installed=`dpkg_version $pkg`
  status=`version_status $ver $installed`

  if [ "$status" != OK ]; then
    colorize_output install
    sudo apt-get -y --force-yes install $pkg
    installed=`dpkg_version $pkg`
    status=`version_status $ver $installed`
  fi

  report_status $pkg $ver $status $installed
}

function checkprop {
  pkg=$1
  ver=$2

  ipath=`fgrep $1.home gradle.properties 2> /dev/null | awk '{print $3}'`
  installed=`ls -ld $ipath`  # Use -l to follow symbolic links
  installed=${installed##* }
  installed=${installed##*/}

  if [ "$installed" == "$pkg" ]; then
    true # Do nothing -- leave just the package name
  else
    installed=${installed##$pkg}
    # Extract a version number, removing leading and traling non-numbers.
    installed=`echo $installed | sed -e 's/[^0-9]*\([0-9].*[0-9]\)[^0-9]*/\1/'`
    installed=${installed%.}  # For case when it is missing.
  fi

  status=`version_status $ver $installed`  

  report_status $pkg $ver $status $installed
}

function extract_property {
  foo=`fgrep $1 $2 2> /dev/null`
  echo ${foo#*=}
}

function check_android {
  pkg=$1
  ver=$2
  if [ "$pkg" == "sdk" ]; then
    dir=`extract_property android.sdk.home gradle.properties`
    installed=`extract_property Pkg.Revision $dir/tools/source.properties`
  else
    installed=`extract_property android.platform gradle.properties`
  fi
  status=`version_status $ver $installed`  

  report_status android-$pkg $ver $status $installed
}

function check_ros {
  pkg=$1
  ver=$2
  if [ "$pkg" == "main" ]; then
    installed=$ROS_DISTRO
    status=`version_status $ver $installed`  
  else # pkg = path
    if [ "$ROS_PACKAGE_PATH" == "" ]; then
      installed=
      status=MISSING
    else
      installed=${ROS_PACKAGE_PATH%%:*}
      if [ "$installed" == "$ver" ]; then
        status=OK
      else
        status=BAD
      fi
    fi
  fi

  report_status ros-$pkg $ver $status $installed
}


function check_gradle {
  pkg=$1
  ver=$2
  installed=`(gradle -v | fgrep -v "build time" | grep "^$pkg" | awk '{print $2}') 2> /dev/null`
  status=`version_status $ver $installed`  

  report_status gradle-$pkg $ver $status $installed
}

function check_maven {
  pkg=$1
  ver=$2
  if [ -d "$ver" ]; then
    installed=`ls -d $ver`
    if [ -d "$installed/$pkg" ]; then
      status=OK
    else
      status=BAD
    fi
  else
    installed=
    status=MISSING
  fi

  report_status maven-$pkg $ver $status $installed
}

function mvnsub {
  SUBDIR=interactivespaces_build/$1
  shift

  (cd $SUBDIR; mvn $@)
}

