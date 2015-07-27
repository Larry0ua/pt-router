package dospring.storage.parser

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

class OsmTransportParser extends DefaultHandler {
    String filename
    Closure processNode
    Closure processWay
    Closure processRelation

    public void process() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(filename, this)
    }

    Map<String, String> element
    Map<String, String> tags
    List<Member> members
    List<Long> nodes

    @Override
    void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (localName in ['node', 'relation', 'way']) {
            element = [:]
            tags = [:]
            members = []
            nodes = []
            for (int i = 0; i < attributes.length; i++) {
                element[attributes.getLocalName(i)] = attributes.getValue(i)
            }
            element._type = localName
        } else if (localName == 'member') {
            members << new Member(
                    ref: attributes.getValue('', 'ref').toLong(),
                    type: attributes.getValue('', 'type'),
                    role: attributes.getValue('', 'role'),
            )
        } else if (localName == 'tag') {
            tags.put(   attributes.getValue('', 'k'),
                        attributes.getValue('', 'v'))
        } else if (localName == 'nd') {
            nodes.add(attributes.getValue('ref').toLong())
        }
    }

    @Override
    void endElement(String uri, String localName, String qName) throws SAXException {
        if (localName == 'node') {
            processNode(element, tags)
        } else if (localName == 'way') {
            processWay(element, tags, nodes);
        } else if (localName == 'relation') {
            processRelation(element, tags, members)
        }
    }
}

class Member {
    Long ref
    String type
    String role
}

