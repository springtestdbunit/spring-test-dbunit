package com.github.springtestdbunit.testutils;

import com.github.springtestdbunit.entity.OtherSampleEntity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by oleksii on 02.08.2014.
 */
public class OtherEntityInitializer implements InitializingBean {
    // single TransactionTemplate shared amongst all methods in this instance
    private final TransactionTemplate transactionTemplate;
    @PersistenceContext
    EntityManager entityManager;

    // use constructor-injection to supply the PlatformTransactionManager
    @Autowired
    public OtherEntityInitializer(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void afterPropertiesSet() throws Exception {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                OtherSampleEntity entity = new OtherSampleEntity();
                entity.setValue("fromEntityManager");
                entityManager.persist(entity);
            }
        });
    }
}
