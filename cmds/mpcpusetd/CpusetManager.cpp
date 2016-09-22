// vim: ts=4 sw=4 expandtab

//#define CPUSET_DEBUG 1
#ifdef CPUSET_DEBUG
#define LOG_NDEBUG 0
#endif

#define LOG_TAG "CpusetManager"
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <stdlib.h>
#include <cutils/log.h>
#include <cutils/fs.h>
#include <android-base/stringprintf.h>

#include "CpusetManager.h"

#define CPUSET_ROOT "/dev/cpuset/cpus"
#define CPUSET_FG "/dev/cpuset/foreground/cpus"
#define CPUSET_FG_BOOST "/dev/cpuset/foreground/boost/cpus"
#define CPUSET_BG "/dev/cpuset/background/cpus"
#define CPUSET_BG_SYSTEM "/dev/cpuset/system-background/cpus"
#define CPUSET_TOP_APP "/dev/cpuset/top-app/cpus"

#define SYSFS_CPU_ONLINE "/sys/devices/system/cpu/online"

CpusetManager::CpusetManager() {
    mMaxCpus = sysconf(_SC_NPROCESSORS_CONF);
    CPU_ZERO(&mCpuSet);
}

CpusetManager::~CpusetManager() {
}

int CpusetManager::start() {
    if (access(SYSFS_CPU_ONLINE, R_OK) != 0) {
        SLOGE("cannot open %s file: %s", SYSFS_CPU_ONLINE, strerror(errno));
        return -1;
    }
    updateCpuset();
    return 0;
}

int CpusetManager::stop() {
    return 0;
}

CpusetManager* CpusetManager::sInstance = NULL;
CpusetManager* CpusetManager::Instance() {
    if (!sInstance) {
        sInstance = new CpusetManager();
    }
    return sInstance;
}

/**
 * copy from android_util_Process.cpp
 * Sample CPUset list format:
 *  0-3,4,6-8
 */
static void parse_cpuset_cpus(char *cpus, cpu_set_t *cpu_set) {
    unsigned int start, end, matched, i;
    char *cpu_range = strtok(cpus, ",");
    while (cpu_range != NULL) {
        start = end = 0;
        matched = sscanf(cpu_range, "%u-%u", &start, &end);
        cpu_range = strtok(NULL, ",");
        if (start >= CPU_SETSIZE) {
            SLOGE("parse_cpuset_cpus: ignoring CPU number larger than %d.", CPU_SETSIZE);
            continue;
        } else if (end >= CPU_SETSIZE) {
            SLOGE("parse_cpuset_cpus: ignoring CPU numbers larger than %d.", CPU_SETSIZE);
            end = CPU_SETSIZE - 1;
        }
        if (matched == 1) {
            CPU_SET(start, cpu_set);
        } else if (matched == 2) {
            for (i = start; i <= end; i++) {
                CPU_SET(i, cpu_set);
            }
        } else {
            SLOGE("Failed to match cpus");
        }
    }
    return;
}

void CpusetManager::printCpuset(const char *filename, char* out, int outsize) {
    int fd = open(filename, O_RDONLY);
    if (fd == -1) {
        out[0] = '\0';
        return;
    }
    ssize_t rsize = read(fd, out, outsize);
    if (rsize <= 0) {
        out[0] = '\0';
        close(fd);
        return;
    }
    out[rsize - 1] = '\0';
    close(fd);
}

void CpusetManager::setCpuset(const char *filename, int cpus) {
#ifdef CPUSET_DEBUG
    char old_mask[64], new_mask[64];
    printCpuset(filename, old_mask, sizeof(old_mask));
#endif

    int fd = open(filename, O_WRONLY);
    if (fd < 0) {
        SLOGE("open %s failed: %s", filename, strerror(errno));
        return;
    }

    int busy = 0;
    std::string cpumask;

#define MAX(a, b) ((a) > (b) ? (a) : (b))
    cpus = MAX(1, cpus);

    int added = 0;

    for (int i = mMaxCpus-1; i >= 0; i--) {
        if (CPU_ISSET(i, &mCpuSet)) {
            android::base::StringAppendF(&cpumask, "%d,", i);
            added++;
            if (cpus == added) {
                break;
            }
        }
    }

    int saved_errno = 0;

    if (write(fd, cpumask.c_str(), cpumask.length()) < 0) {
        saved_errno = errno;
        SLOGE("write error to %s: %s", filename, strerror(saved_errno));
    }
    close(fd);

#ifdef CPUSET_DEBUG
    {
        printCpuset(filename, new_mask, sizeof(new_mask));
        if (saved_errno != 0) {
            SLOGE("write: %s => %s but %s to %s", old_mask, cpumask.c_str(), new_mask, filename);
        } else {
            SLOGV("write %s => %s to %s", old_mask, cpumask.c_str(), filename);
        }
    }
#endif
}

void CpusetManager::updateCpuset() {
    cpu_set_t cpu_set;
    char *line = NULL;
    size_t len = 0;
    int online = 0;
    CPU_ZERO(&cpu_set);

    FILE *fp = fopen(SYSFS_CPU_ONLINE, "re");
    if (fp == NULL) {
        SLOGE("cannot open %s file: %s", SYSFS_CPU_ONLINE, strerror(errno));
        return;
    }
    ssize_t num_read = getline(&line, &len, fp);
    fclose(fp);

    if (num_read > 0) {
        parse_cpuset_cpus(line, &cpu_set);
    } else {
        SLOGE("getline caught error: %s", strerror(errno));
        free(line);
        return;
    }
    free(line);

    if (CPU_EQUAL(&mCpuSet, &cpu_set)) {
        return;
    }

    memcpy(&mCpuSet, &cpu_set, sizeof(mCpuSet));

    online = CPU_COUNT(&mCpuSet);

    setCpuset(CPUSET_TOP_APP, online);
    setCpuset(CPUSET_BG_SYSTEM, online *3/4);
    setCpuset(CPUSET_BG, 1);
    // XXX: set fg twice to avoid EBUSY
    setCpuset(CPUSET_FG, online);
    setCpuset(CPUSET_FG, online *3/4);
    setCpuset(CPUSET_FG_BOOST, online *3/4);
}

void CpusetManager::handleCpuEvent(NetlinkEvent *event) {
    updateCpuset();
}
