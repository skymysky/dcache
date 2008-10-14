package org.dcache.pool.migration;

import java.util.Collection;
import org.dcache.cells.CellStub;
import diskCacheV111.vehicles.PoolManagerGetPoolsByLinkMessage;
import diskCacheV111.vehicles.PoolManagerGetPoolsMessage;

class PoolListByLink
    extends PoolListFromPoolManager
{
    private final CellStub _poolManager;
    private final String _link;

    public PoolListByLink(CellStub poolManager,
                          Collection<String> exclude,
                          double spaceFactor,
                          double cpuFactor,
                          String link)
    {
        super(exclude, spaceFactor, cpuFactor);
        _poolManager = poolManager;
        _link = link;
    }

    public void refresh()
    {
        _poolManager.send(new PoolManagerGetPoolsByLinkMessage(_link),
                          PoolManagerGetPoolsMessage.class,
                          this);
    }

    public String toString()
    {
        return String.format("link %s, %d pools", _link, _pools.size());
    }
}
