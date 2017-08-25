#!/bin/bash
FOLDER_LIGHT="/home/tassadar/Android_Design_Icons_20120229/All_Icons/holo_light/"
FOLDER_DARK="/home/tassadar/Android_Design_Icons_20120229/All_Icons/holo_dark/"
DARK=1
FOLDER=""
DPI_LIST=( mdpi hdpi xhdpi )

for arg in "$@"
do
    if [ $arg == "-l" ] ; then
        DARK=0
    elif [ $arg == "-d" ] ; then
        DARK=1
    fi
done

if [ $DARK -eq 1 ] ; then
    echo "Using dark holo"
    FOLDER=$FOLDER_DARK
else
    echo "Using light holo"
    FOLDER=$FOLDER_LIGHT
fi

echo ""

for arg in "$@"
do
    if [ $arg == "-l" ] || [ $arg == "-d" ] ; then
        continue
    fi

    new_name="$(echo $arg | cut -d'-' -f2-999 | tr '-' '_')"
    echo "$arg to $new_name..."
    for dpi in ${DPI_LIST[@]}
    do
        cmd="cp $FOLDER$dpi/$arg /home/tassadar/VNote/res/drawable-$dpi/$new_name"
        echo "$cmd"
        $($cmd)
    done
done
