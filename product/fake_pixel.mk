PRODUCT_PACKAGE_OVERLAYS := \
	vendor/fake_nexus/product/overlay-pixel \

PRODUCT_PACKAGES := \
	PixelSoundResources \
	NexusLauncher \
	SystemUIGoogle \
	SettingsGoogle \

PRODUCT_PROPERTY_OVERRIDES := \
	ro.config.ringtone=Zen.ogg \
	ro.config.notification_sound=Chime.ogg \
	ro.config.alarm_alert=Flow.ogg \

PRODUCT_COPY_FILES := vendor/fake_nexus/data/media/bootanimation.zip:system/media/bootanimation.zip:fake_nexus \
	vendor/fake_nexus/data/data-pixel/nexus.xml:system/etc/sysconfig/nexus.xml:fake_nexus \
