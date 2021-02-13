package brackettree.reader;


import suite.suite.Subject;
import suite.suite.Vendor;

public class FactoryVendorRoot extends Vendor {

    ObjectFactory factory;

    public FactoryVendorRoot(ObjectFactory factory, Subject $local) {
        super($local);
        this.factory = factory;
    }

    @Override
    protected Subject wrap(Subject subject) {
        return factory.factoryVendor(subject);
    }

    @Override
    protected Subject factor(Subject subject) {
        return subject;
    }

    @Override
    protected Subject factor(Subject subject, Class<?> aClass) {
        return subject;
    }
}
