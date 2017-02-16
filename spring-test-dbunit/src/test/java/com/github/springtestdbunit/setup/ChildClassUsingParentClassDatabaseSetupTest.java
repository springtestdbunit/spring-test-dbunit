package com.github.springtestdbunit.setup;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.springtestdbunit.entity.EntityAssert;

public class ChildClassUsingParentClassDatabaseSetupTest extends ParentClassContainingDatabaseSetup {

    @Autowired
    private EntityAssert entityAssert;

    @Test
    public void test() throws Exception {
        this.entityAssert.assertValues("fromDbUnit");
    }

}
