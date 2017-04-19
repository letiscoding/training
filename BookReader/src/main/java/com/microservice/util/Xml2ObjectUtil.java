package com.microservice.util;

import org.apache.log4j.Logger;
import org.castor.mapping.BindingType;
import org.castor.mapping.MappingUnmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingLoader;
import org.exolab.castor.xml.ClassDescriptorResolver;
import org.exolab.castor.xml.ClassDescriptorResolverFactory;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.XMLClassDescriptorResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xml2ObjectUtil {

	private static final Logger logger = Logger.getLogger(Xml2ObjectUtil.class);

	private static HashMap<URL, ClassDescriptorResolver> resolverMap = new HashMap<URL, ClassDescriptorResolver>();

	private static synchronized ClassDescriptorResolver getResolver(URL url) {
		if (url == null)
			return null;
		try {

			ClassDescriptorResolver resolver = ClassDescriptorResolverFactory
					.createClassDescriptorResolver(BindingType.XML);
			Mapping mapping = new Mapping(Xml2ObjectUtil.class.getClassLoader());
			mapping.loadMapping(url);
			MappingUnmarshaller unmarshaller = new MappingUnmarshaller();
			MappingLoader mappingLoader = unmarshaller.getMappingLoader(
					mapping, BindingType.XML);
			resolver.setMappingLoader(mappingLoader);

			resolverMap.put(url, resolver);

			return resolver;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
		}

		return null;
	}

	public static synchronized <T> T xml2Object(Class<T> objClass,
			String xmlFile) throws Exception {
		if (objClass == null || xmlFile == null || xmlFile.trim().equals(""))
			return null;

		InputStream in = Xml2ObjectUtil.class.getClassLoader()
				.getResourceAsStream(xmlFile);
		return xml2Object(objClass, in);
	}

	public static synchronized <T> T xml2Object(Class<T> objClass,
			String mapFile, String xmlFile) throws Exception {
		if (objClass == null || xmlFile == null)
			return null;
		URL mapping = null;
		if (mapFile != null && !mapFile.trim().equals(""))
			mapping = objClass.getClassLoader().getResource(mapFile);
		InputStream in = Xml2ObjectUtil.class.getClassLoader()
				.getResourceAsStream(xmlFile);
		return xml2Object(objClass, mapping, in);
	}

	@SuppressWarnings("unchecked")
	public static synchronized <T> T xml2Object(Class<T> objClass, URL mapping,
			InputStream input) throws Exception {
		if (objClass == null || input == null)
			return null;
		if (mapping == null)
			mapping = getDefaultMap(objClass);

		ClassDescriptorResolver resolver = getResolver(mapping);

		Unmarshaller unmarshaller = new Unmarshaller();
		unmarshaller.setWhitespacePreserve(false);
		unmarshaller.setResolver((XMLClassDescriptorResolver) resolver);
		unmarshaller.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				if (systemId != null && !systemId.trim().equals("")) {
					File file = new File(systemId);
					InputStream input = Xml2ObjectUtil.class.getClassLoader()
							.getResourceAsStream("dtd/" + file.getName());
					return new InputSource(input);
				}
				return null;
			}
		});

		InputStreamReader reader = new InputStreamReader(input);
		return (T) unmarshaller.unmarshal(reader);
	}

	public static synchronized <T> T xml2Object(Class<T> objClass,
			InputStream input) throws Exception {
		return xml2Object(objClass, null, input);

	}

	private static synchronized URL getDefaultMap(Class<?> objClass) {
		if (objClass == null)
			return null;
		String path = objClass.getName();
		path = path.replace('.', '/');
		String mapFile = path + ".xml";
		URL url = objClass.getClassLoader().getResource(mapFile);
		if (url != null) {
			logger.debug("getDefaultMap url:" + url.toString() + ", "
					+ url.getFile());
		}

		return url;
	}

	public static synchronized void object2Xml(Object obj, String xmlFile,
			String encode) throws Exception {
		if (obj == null || xmlFile == null || xmlFile.trim().equals(""))
			return;
		object2Xml(obj, new FileOutputStream(xmlFile), encode);
	}

	public static synchronized void object2Xml(Object obj, String mapFile,
			String xmlFile, String encode) throws Exception {
		if (obj == null || xmlFile == null)
			return;
		URL mapping = null;
		if (mapFile != null && !mapFile.trim().equals(""))
			mapping = obj.getClass().getClassLoader().getResource(mapFile);
		object2Xml(obj, mapping, new FileOutputStream(xmlFile), encode);
	}

	public static synchronized void object2Xml(Object obj, OutputStream out,
			String encode) throws Exception {
		if (obj == null || out == null)
			return;
		object2Xml(obj, null, out, encode);
	}

	public static synchronized void object2Xml(Object obj, URL mapping,
			OutputStream out, String encode) throws Exception {
		if (obj == null || out == null)
			return;
		if (mapping == null)
			mapping = getDefaultMap(obj.getClass());

		ClassDescriptorResolver resolver = getResolver(mapping);

		OutputStreamWriter writer = new OutputStreamWriter(out);
		Marshaller marshaller = new Marshaller(writer);
		if (encode != null && !encode.trim().equals(""))
			marshaller.setEncoding(encode);
		marshaller.setResolver((XMLClassDescriptorResolver) resolver);
		marshaller.marshal(obj);
	}
}