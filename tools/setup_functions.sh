
function colorize_output {
  if [ "$1" == "reset" ]; then
    tput setaf 7
  elif [ "$1" == "OK" ]; then
    tput setaf 2
  else
    tput setaf 5
  fi
}

function dpkg_version {
  dpkg -l | grep " $1 " | awk '{print $3}'
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

function package {
  pkg=$1
  ver=$2
  url=$3

  installed=`dpkg_version $pkg`
  status=`version_status $ver $installed`

  colorize_output $status
  echo $pkg $ver ':' $installed $status
}

function download {
  pkg=$1
  ver=$2
  postfix=$3
  url=$4

  ipath=`fgrep $1.home gradle.properties | awk '{print $3}'`
  installed=`ls -d $ipath`
  installed=${installed##*/$pkg-}
  installed=${installed%%$postfix}
  status=`version_status $ver $installed`  

  colorize_output $status
  echo $pkg $ver ':' $installed $status
}

function extract_property {
  foo=`fgrep $1 $2`
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
  colorize_output $status
  echo android-$pkg $ver ':' $installed $status
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
  colorize_output $status
  echo ros-$pkg $ver ':' $installed $status
}


function check_gradle {
  pkg=$1
  ver=$2
  installed=`gradle -v | fgrep -v "build time" | grep "^$pkg" | awk '{print $2}'`
  status=`version_status $ver $installed`  
  colorize_output $status
  echo gradle-$pkg $ver ':' $installed $status
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
  colorize_output $status
  echo maven-$pkg $ver ':' $installed $status
}

function check_is {
  pkg=$1
  ver=$2
  prefix=interactivespaces-launcher-
  installed=`ls -d $ISDIR/$pkg/$prefix*.jar`
  installed=${installed%.jar}
  installed=${installed#*$prefix}
  status=`version_status $ver $installed`  
  colorize_output $status
  echo is-$pkg $ver ':' $installed $status
}

