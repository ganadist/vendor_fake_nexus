kernel_build() {
  $(gettop)/vendor/fake_nexus/build/kernel_build "$@"
}

cleanodex () {
  $(gettop)/vendor/fake_nexus/build/cleanodex "$@"
}

prepare() {
  $(gettop)/vendor/fake_nexus/build/prepare "$@"
}

