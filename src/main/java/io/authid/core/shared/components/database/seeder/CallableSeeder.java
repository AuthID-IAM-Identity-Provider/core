package io.authid.core.shared.components.database.seeder;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class CallableSeeder extends Seeder implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @SafeVarargs
    protected final void call(Class<? extends Seeder>... seederClasses) {
        for (Class<? extends Seeder> seederClass : seederClasses) {
            Seeder seeder = applicationContext.getBean(seederClass);
            seeder.run();
        }
    }
}
