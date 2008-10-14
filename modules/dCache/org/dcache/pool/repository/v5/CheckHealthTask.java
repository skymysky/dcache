package org.dcache.pool.repository.v5;

import org.apache.log4j.Logger;
import org.dcache.pool.FaultAction;
import org.dcache.pool.repository.SpaceRecord;

class CheckHealthTask implements Runnable
{
    private static Logger _log = Logger.getLogger(CheckHealthTask.class);

    private final CacheRepositoryV5 _repository;

    CheckHealthTask(CacheRepositoryV5 repository)
    {
        _repository = repository;
    }

    public void run()
    {
        if (!_repository.isRepositoryOk()) {
            _repository.fail(FaultAction.DISABLED, "I/O test failed");
        }

        if (!checkSpaceAccounting()) {
            _log.error("Marking pool read-only due to accounting errors. This is a bug. Please report it to support@dcache.org.");
            _repository.fail(FaultAction.READONLY, "Accounting errors detected");
        }
    }

    private boolean checkSpaceAccounting()
    {
        SpaceRecord record = _repository.getSpaceRecord();
        long removable = record.getRemovableSpace();
        long total = record.getTotalSpace();
        long free = record.getFreeSpace();
        long precious = record.getPreciousSpace();
        long used = total - free;

        if (removable < 0) {
            _log.error("Removable space is negative.");
            return false;
        }

        if (total < 0) {
            _log.error("Repository size is negative.");
            return false;
        }

        if (free < 0) {
            _log.error("Free space is negative.");
            return false;
        }

        if (precious < 0) {
            _log.error("Precious space is negative.");
            return false;
        }

        if (used < 0) {
            _log.error("Used space is negative.");
            return false;
        }

        /* The following check cannot be made consistently, since we
         * do not retrieve these values atomically. Therefore we log
         * the error, but do not return false.
         */
        if (precious + removable > used) {
            _log.warn("Used space is less than the sum of precious and removable space (this may be a temporary problem - if it persists then please report it to support@dcache.org).");
        }

        return true;
    }
}