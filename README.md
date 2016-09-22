# Fake Nexus Rom

## Introduction

This repository is collection of build scripts for Fake Nexus Rom.

XDA thread: http://forum.xda-developers.com/nexus-4/development/fake-nexus-rom-nexus-4-t3230268

## Build

### Preparing sources

First, please see [Requirements](http://s.android.com/source/requirements.html) and [Establishing a Build Environment](http://s.android.com/source/initializing.html) carefully.

After prepare build environment, you can download android source code from AOSP site.
To initialize for Nexus 4 android source, you must run this command


    $ mkdir ~/bin
    $ PATH=~/bin:$PATH
    $ curl https://storage.googleapis.com/git-repo-downloads/repo > ~/bin/repo
    $ chmod a+x ~/bin/repo
    $ mkdir -p ~/src/build/android
    $ cd ~/src/build/android
    $ repo init -u https://android.googlesource.com/platform/manifest -b android-7.0.0_r4 -p linux
    

After initialize repositories, add local_manifests with following commands

    $ git clone -b nougat-mako-dev https://github.com/ganadist/fake_nexus_local_manifests .repo/local_manifests

And run this command

    $ repo sync -j5

### Configure build environment for Nexus 4

run following command from android top directory

    $ source build/envsetup.sh
    including device/lge/mako/vendorsetup.sh
    including device/lge/occam/vendorsetup.sh
    please prepare with following commands
     $ lunch occam-user
     $ kernel_build
     $ prepare
     $ make updatepackage
    including vendor/google/build/vendorsetup.sh
    including vendor/fake_nexus/build/vendorsetup.sh

    $ lunch occam-user

    ============================================
    PLATFORM_VERSION_CODENAME=REL
    PLATFORM_VERSION=7.0
    TARGET_PRODUCT=occam
    TARGET_BUILD_VARIANT=user
    TARGET_BUILD_TYPE=release
    TARGET_BUILD_APPS=
    TARGET_ARCH=arm
    TARGET_ARCH_VARIANT=armv7-a-neon
    TARGET_CPU_VARIANT=krait
    TARGET_2ND_ARCH=
    TARGET_2ND_ARCH_VARIANT=
    TARGET_2ND_CPU_VARIANT=
    HOST_ARCH=x86_64
    HOST_2ND_ARCH=x86
    HOST_OS=linux
    HOST_OS_EXTRA=Linux-4.7.2-1-ARCH-x86_64-with-glibc2.2.5
    HOST_CROSS_OS=windows
    HOST_CROSS_ARCH=x86
    HOST_CROSS_2ND_ARCH=x86_64
    HOST_BUILD_TYPE=release
    BUILD_ID=NRD90M
    OUT_DIR=out
    ============================================

### build kernel

You can build kernel binary with following command

    vendor/fake_nexus/build/kernel_build

### apply several patches for this rom

You can apply several patches with following command

    vendor/fake_nexus/build/prepare

### prepare gapps

This source repository do not have gapps binaries.

