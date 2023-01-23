package com.domainsurvey.crawler.utils.diff;

import java.io.IOException;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.xml.sax.ContentHandler;

public class XslFilter {

    public ContentHandler xsl(ContentHandler consumer, String xslPath) throws IOException {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();

            Resource resource = new ClassPathResource(xslPath);

            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(resource.getInputStream()));

            // Use the template to create a transformer
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTransFact = (SAXTransformerFactory) transFact;
            // create a ContentHandler
            TransformerHandler transHand = saxTransFact.newTransformerHandler(template);

            transHand.setResult(new SAXResult(consumer));

            return transHand;

        } catch (TransformerConfigurationException | IllegalArgumentException | TransformerFactoryConfigurationError e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Can't transform xml.");

    }

}
