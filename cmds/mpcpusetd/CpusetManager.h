// vim: ts=4 sw=4 expandtab
#ifndef __CPUSETMANAGER_H__
#define __CPUSETMANAGER_H__

// To make sure cpu_set_t is included from sched.h
#define _GNU_SOURCE 1
#include <sched.h>

#include <sysutils/NetlinkEvent.h>

class CpusetManager {
private:
    static CpusetManager *sInstance;
    CpusetManager();

    size_t mMaxCpus;
    cpu_set_t mCpuSet;
    void setCpuset(const char *filename, int cpus);
    void printCpuset(const char *filename, char* out, int outsiz);
    void updateCpuset();

public:
    virtual ~CpusetManager();
    int start();
    int stop();
    void handleCpuEvent(NetlinkEvent *event);

    static CpusetManager *Instance();
};

#endif
