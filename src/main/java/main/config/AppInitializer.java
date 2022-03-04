//package main.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
//
//public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
//
//    @Autowired
//    private YAMLConfig config;
//
//    @Override
//    protected Class<?>[] getRootConfigClasses() {
//        return new Class[0];
//    }
//
//    @Override
//    protected Class<?>[] getServletConfigClasses() {
//        return new Class[]{WebConfig.class};
//    }
//
//    @Override
//    protected String[] getServletMappings() {
//        return new String[]{config.getWebInterface()};
//    }
//}
