# Choose a product to build for.  Look in the products directory for ones
# that work.
ifndef TARGET_PRODUCT
#TARGET_PRODUCT:=generic
endif

# Choose a variant to build.  If you don't pick one, the default is eng.
# User is what we ship.  Userdebug is that, with a few flags turned on
# for debugging.  Eng has lots of extra tools for development.
ifndef TARGET_BUILD_VARIANT
#TARGET_BUILD_VARIANT:=user
TARGET_BUILD_VARIANT:=userdebug
#TARGET_BUILD_VARIANT:=eng
endif

# If you have a special place to put your ouput files, set this, otherwise
# it goes to <build-root>/out
ifndef OUT_DIR
OUT_DIR:=out-$(TARGET_PRODUCT)-$(TARGET_BUILD_VARIANT)
endif

# when the build system changes such that this file must be updated, this
# variable will be changed.  After you have modified this file with the new
# changes (see buildspec.mk.default), update this to the new value from
# buildspec.mk.default.
BUILD_ENV_SEQUENCE_NUMBER := 10

BUILD_NUMBER=$(shell date '+%Y%m%d')
