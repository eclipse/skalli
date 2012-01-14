/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.skalli.core.internal.validation;

import org.eclipse.skalli.commons.FormatUtils;
import org.eclipse.skalli.model.EntityBase;
import org.eclipse.skalli.services.validation.Validation;

class QueuedEntity<T extends EntityBase> extends Validation<T> implements Comparable<QueuedEntity<T>> {
    private long queuedAt = -1L;
    private long startedAt = -1L;
    private long nanos = -1L; // ensure well-defined ordering in queue

    private static long NANO_TIME = System.nanoTime();

    public QueuedEntity(Validation<T> validation) {
        super(validation.getEntityClass(), validation.getEntityId(),
                validation.getMinSeverity(), validation.getUserId(), validation.getPriority());
        queuedAt = System.currentTimeMillis();
        nanos = System.nanoTime() - NANO_TIME;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    public long getQueuedAt() {
        return queuedAt;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (queuedAt >= 0) {
            sb.append("[queued at ").append(FormatUtils.formatUTCWithMillis(queuedAt)).append("]");
        }
        if (startedAt >= 0) {
            sb.append("[started at ").append(FormatUtils.formatUTCWithMillis(startedAt)).append("]");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(QueuedEntity<T> o) {
        int result = getPriority() < o.getPriority() ? -1 : (getPriority() == o.getPriority() ? 0 : 1);
        if (result == 0) {
            result = nanos < o.nanos ? -1 : (nanos == o.nanos ? 0 : 1);
        }
        return result;
    }
}