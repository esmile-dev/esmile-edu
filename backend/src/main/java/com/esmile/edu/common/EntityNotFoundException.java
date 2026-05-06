package com.esmile.edu.common;

import com.esmile.edu.common.exception.ResourceNotFoundException;

/**
 * @deprecated Use {@link ResourceNotFoundException} instead.
 */
@Deprecated
public class EntityNotFoundException extends ResourceNotFoundException {

    public EntityNotFoundException(String message) {
        super("Entity", message);
    }
}
