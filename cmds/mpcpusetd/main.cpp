// vim: ts=4 sw=4 expandtab

#include <sched.h>
#include <sys/resource.h>
#include <android-base/logging.h>
#include <android-base/stringprintf.h>
#include <cutils/sockets.h>

#include "CpusetManager.h"
#include "NetlinkManager.h"

int main(int argc, char** argv) {
    setenv("ANDROID_LOG_TAGS", "*:v", 1);
    android::base::InitLogging(argv, android::base::LogdLogger(android::base::SYSTEM));

    setpriority(PRIO_PROCESS, 0, -20);

    struct sched_param param = {
            .sched_priority = 1,
    };
    sched_setscheduler(0, SCHED_FIFO, &param);

    LOG(INFO) << "mpcpusetd 0.1 fireing up";

    CpusetManager *cm;
    NetlinkManager *nm;

    if (!(cm = CpusetManager::Instance())) {
        LOG(ERROR) << "Unable to create CpusetManager";
        exit(1);
    }

    if (!(nm = NetlinkManager::Instance())) {
        LOG(ERROR) << "Unable to create NetlinkManager";
        exit(1);
    }

    if (cm->start()) {
        PLOG(ERROR) << "Unable to start CpusetManager";
        goto loop;
    }
    if (nm->start()) {
        PLOG(ERROR) << "Unable to start NetlinkHandler";
        goto loop;
    }

loop:

    while(1) {
        sleep(1000);
    }

    LOG(ERROR) << "mcpusetd exiting";
    exit(0);
}

