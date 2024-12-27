package io.zeebe.monitor.zeebe.status;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.stereotype.Component;

@Component
public class ZeebeStatusKeeper {

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private volatile ClusterStatus status = new ClusterStatus();

  public ClusterStatus getStatus() {
    try {
      lock.readLock().lock();
      return status;
    } finally {
      lock.readLock().unlock();
    }
  }

  public void setStatus(ClusterStatus status) {
    try {
      lock.writeLock().lock();
      this.status = status;
    } finally {
      lock.writeLock().unlock();
    }
  }
}
