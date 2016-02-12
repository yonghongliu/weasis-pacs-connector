package org.weasis.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.weasis.dicom.data.xml.TagUtil;
import org.weasis.dicom.util.StringUtil;

public class ConnectorProperties extends Properties {
    public static final String CONFIG_FILENAME = "config.filename";
    
    private final List<Properties> list;

    public ConnectorProperties() {
        list = new ArrayList<Properties>();
    }

    public ConnectorProperties(Properties defaults) {
        super(defaults);
        list = new ArrayList<Properties>();
    }

    public void addArchiveProperties(Properties archiveProps) {
        list.add(archiveProps);
    }

    public List<Properties> getArchivePropertiesList() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public synchronized Object clone() {
        ConnectorProperties newObject = new ConnectorProperties();
        newObject.putAll(this);
        for (Properties properties : list) {
            newObject.list.add((Properties) properties.clone());
        }
        return newObject;
    }

    public ConnectorProperties getResolveConnectorProperties(HttpServletRequest request) {
        Properties extProps = new Properties();
        extProps.put("server.base.url", ServletUtil.getBaseURL(request,
            StringUtil.getNULLtoFalse(this.getProperty("server.canonical.hostname.mode"))));

        ConnectorProperties dynamicProps = (ConnectorProperties) this.clone();

        // Perform variable substitution for system properties.
        for (Enumeration<?> e = this.propertyNames(); e.hasMoreElements();) {
            String name = (String) e.nextElement();
            dynamicProps.setProperty(name, TagUtil.substVars(this.getProperty(name), name, null, this, extProps));
        }

        dynamicProps.putAll(extProps);

        for (Properties dynProps : dynamicProps.list) {
            // Perform variable substitution for system properties.
            for (Enumeration<?> e = dynProps.propertyNames(); e.hasMoreElements();) {
                String name = (String) e.nextElement();
                dynProps.setProperty(name,
                    TagUtil.substVars(dynProps.getProperty(name), name, null, dynProps, extProps));
            }
        }
        return dynamicProps;

    }
}
