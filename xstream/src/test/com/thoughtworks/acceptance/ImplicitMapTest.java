/*
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2018 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 * Created on 05. August 2011 Joerg Schaible
 */
package com.thoughtworks.acceptance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.thoughtworks.acceptance.objects.Hardware;
import com.thoughtworks.acceptance.objects.Product;
import com.thoughtworks.acceptance.objects.SampleMaps;
import com.thoughtworks.acceptance.objects.Software;
import com.thoughtworks.xstream.converters.collections.MapConverter;


public class ImplicitMapTest extends AbstractAcceptanceTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xstream.registerConverter(new MapConverter(xstream.getMapper()) {
            @Override
            public boolean canConvert(final Class<?> type) {
                return type == LinkedHashMap.class;
            }
        });
        xstream.addDefaultImplementation(LinkedHashMap.class, Map.class);
        xstream.alias("sample", SampleMaps.class);
        xstream.alias("software", Software.class);
        xstream.alias("hardware", Hardware.class);
        xstream.alias("product", Product.class);
        xstream.alias("sample2", SampleMaps2.class);
        xstream.alias("sample3", SampleMaps3.class);
        xstream.ignoreUnknownElements();
    }

    public void testWithout() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <good>\n"
            + "    <entry>\n"
            + "      <string>Windows</string>\n"
            + "      <software>\n"
            + "        <vendor>Microsoft</vendor>\n"
            + "        <name>Windows</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "    <entry>\n"
            + "      <string>Linux</string>\n"
            + "      <software>\n"
            + "        <vendor>Red Hat</vendor>\n"
            + "        <name>Linux</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "  </good>\n"
            + "  <bad/>\n"
            + "</sample>";

        assertBothWays(sample, expected);
    }

    public void testWithMap() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithReferencedImplicitElement() {
        final List<Object> list = new ArrayList<>();
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));
        list.add(sample.good.get("Windows"));
        list.add(sample);
        list.add(sample.good.get("Linux"));

        final String expected = ""
            + "<list>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <sample>\n"
            + "    <software reference=\"../../software\"/>\n"
            + "    <software>\n"
            + "      <vendor>Red Hat</vendor>\n"
            + "      <name>Linux</name>\n"
            + "    </software>\n"
            + "    <bad/>\n"
            + "  </sample>\n"
            + "  <software reference=\"../sample/software[2]\"/>\n"
            + "</list>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(list, expected);
    }

    public static class MegaSampleMaps<OK, OV, GK, GV, BK, BV> extends SampleMaps<GK, GV, BK, BV> {
        private static final long serialVersionUID = 201108L;
        String separator = "---";
        Map<OK, OV> other = new LinkedHashMap<>();
        {
            good = new LinkedHashMap<>();
            bad = new LinkedHashMap<>();
        }
    }

    public void testInheritsImplicitMapFromSuperclass() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final SampleMaps<String, Software, ?, ?> sample = new MegaSampleMaps<>(); // subclass
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<MEGA-sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "  <separator>---</separator>\n"
            + "  <other/>\n"
            + "</MEGA-sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testSupportsInheritedAndDirectDeclaredImplicitMapAtOnce() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final MegaSampleMaps<String, Hardware, String, Software, ?, ?> sample = new MegaSampleMaps<>(); // subclass
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));
        sample.other.put("i386", new Hardware("i386", "Intel"));

        final String expected = ""
            + "<MEGA-sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "  <separator>---</separator>\n"
            + "  <hardware>\n"
            + "    <arch>i386</arch>\n"
            + "    <name>Intel</name>\n"
            + "  </hardware>\n"
            + "</MEGA-sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(MegaSampleMaps.class, "other", Hardware.class, "arch");
        assertBothWays(sample, expected);
    }

    public void testInheritedAndDirectDeclaredImplicitMapAtOnceIsNotDeclarationSequenceDependent() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final MegaSampleMaps<String, Hardware, String, Software, ?, ?> sample = new MegaSampleMaps<>(); // subclass
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));
        sample.other.put("i386", new Hardware("i386", "Intel"));

        final String expected = ""
            + "<MEGA-sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "  <separator>---</separator>\n"
            + "  <hardware>\n"
            + "    <arch>i386</arch>\n"
            + "    <name>Intel</name>\n"
            + "  </hardware>\n"
            + "</MEGA-sample>";

        xstream.addImplicitMap(MegaSampleMaps.class, "other", Hardware.class, "arch");
        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testAllowsSubclassToOverrideImplicitMapInSuperclass() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final SampleMaps<String, Software, ?, ?> sample = new MegaSampleMaps<>(); // subclass
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<MEGA-sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "  <separator>---</separator>\n"
            + "  <other/>\n"
            + "</MEGA-sample>";

        xstream.addImplicitMap(MegaSampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testAllowDifferentImplicitMapDefinitionsInSubclass() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good.put("Google", new Software("Google", "Android"));
        final MegaSampleMaps<String, Hardware, String, Software, ?, ?> megaSample = new MegaSampleMaps<>(); // subclass
        megaSample.good.put("Windows", new Software("Microsoft", "Windows"));
        megaSample.good.put("Linux", new Software("Red Hat", "Linux"));
        megaSample.other.put("i386", new Hardware("i386", "Intel"));

        final List<SampleMaps<String, Software, ?, ?>> list = new ArrayList<>();
        list.add(sample);
        list.add(megaSample);
        final String expected = ""
            + "<list>\n"
            + "  <sample>\n"
            + "    <mobile>\n"
            + "      <vendor>Google</vendor>\n"
            + "      <name>Android</name>\n"
            + "    </mobile>\n"
            + "    <bad/>\n"
            + "  </sample>\n"
            + "  <MEGA-sample>\n"
            + "    <software>\n"
            + "      <vendor>Microsoft</vendor>\n"
            + "      <name>Windows</name>\n"
            + "    </software>\n"
            + "    <software>\n"
            + "      <vendor>Red Hat</vendor>\n"
            + "      <name>Linux</name>\n"
            + "    </software>\n"
            + "    <bad/>\n"
            + "    <separator>---</separator>\n"
            + "    <hardware>\n"
            + "      <arch>i386</arch>\n"
            + "      <name>Intel</name>\n"
            + "    </hardware>\n"
            + "  </MEGA-sample>\n"
            + "</list>";

        xstream.addImplicitMap(SampleMaps.class, "good", "mobile", Software.class, "vendor");
        xstream.addImplicitMap(MegaSampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(MegaSampleMaps.class, "other", Hardware.class, "arch");
        assertBothWays(list, expected);
    }

    public void testDefaultMapBasedOnType() {
        xstream.alias("MEGA-sample", MegaSampleMaps.class);

        final MegaSampleMaps<String, Hardware, String, Software, String, Product> sample = new MegaSampleMaps<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Chrome", new Software("Google", "Chrome"));
        sample.bad.put("iPhone", new Product("iPhone", "i", 399.99));
        sample.other.put("Intel", new Hardware("i386", "Intel"));
        sample.other.put("AMD", new Hardware("amd64", "AMD"));

        final String expected = ""
            + "<MEGA-sample>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Google</vendor>\n"
            + "    <name>Chrome</name>\n"
            + "  </software>\n"
            + "  <product>\n"
            + "    <name>iPhone</name>\n"
            + "    <id>i</id>\n"
            + "    <price>399.99</price>\n"
            + "  </product>\n"
            + "  <separator>---</separator>\n"
            + "  <hardware>\n"
            + "    <arch>i386</arch>\n"
            + "    <name>Intel</name>\n"
            + "  </hardware>\n"
            + "  <hardware>\n"
            + "    <arch>amd64</arch>\n"
            + "    <name>AMD</name>\n"
            + "  </hardware>\n"
            + "</MEGA-sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps.class, "bad", Product.class, "name");
        xstream.addImplicitMap(MegaSampleMaps.class, "other", Hardware.class, "name");
        assertBothWays(sample, expected);
    }

    @SuppressWarnings("unchecked")
    public void testWithEMPTY_MAP() {
        final SampleMaps<?, ?, ?, ?> sample = new SampleMaps<>();
        sample.good = Collections.EMPTY_MAP;
        sample.bad = Collections.EMPTY_MAP;

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps.class, "bad", Software.class, "name");
        assertEquals("<sample/>", xstream.toXML(sample));
    }

    public void testWithEmptyMap() {
        final SampleMaps<?, ?, ?, ?> sample = new SampleMaps<>();
        sample.good = Collections.emptyMap();
        sample.bad = Collections.emptyMap();

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps.class, "bad", Software.class, "name");
        assertEquals("<sample/>", xstream.toXML(sample));
    }

    public void testWithSortedMap() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new TreeMap<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addDefaultImplementation(TreeMap.class, Map.class);
        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithExplicitItemNameMatchingTheNameOfTheFieldWithTheMap() {
        final SampleMaps<?, ?, String, Software> sample = new SampleMaps<>();
        sample.bad = new LinkedHashMap<>();
        sample.bad.put("Windows", new Software("Microsoft", "Windows"));
        sample.bad.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <good/>\n"
            + "  <bad>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </bad>\n"
            + "  <bad>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </bad>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "bad", "bad", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithImplicitNameMatchingTheNameOfTheFieldWithTheMap() {
        final SampleMaps<?, ?, String, Software> sample = new SampleMaps<>();
        sample.bad = new LinkedHashMap<>();
        sample.bad.put("Windows", new Software("Microsoft", "Windows"));
        sample.bad.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <good/>\n"
            + "  <bad>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </bad>\n"
            + "  <bad>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </bad>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "bad", Software.class, "name");
        xstream.alias("bad", Software.class);
        assertBothWays(sample, expected);
    }

    public void testWithAliasedItemNameMatchingTheAliasedNameOfTheFieldWithTheMap() {
        final SampleMaps<?, ?, String, Software> sample = new SampleMaps<>();
        sample.bad = new LinkedHashMap<>();
        sample.bad.put("Windows", new Software("Microsoft", "Windows"));
        sample.bad.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <good/>\n"
            + "  <test>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </test>\n"
            + "  <test>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </test>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "bad", "test", Software.class, "name");
        xstream.aliasField("test", SampleMaps.class, "bad");
        assertBothWays(sample, expected);
    }

    public void testWithNullElement() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put(null, null);
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <null/>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithAliasAndNullElement() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put(null, null);
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <null/>\n"
            + "  <code>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </code>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", "code", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public static class SampleMaps2<GK2, GV2, GK, GV, BK, BV> extends SampleMaps<GK, GV, BK, BV> {
        private static final long serialVersionUID = 201509L;
        @SuppressWarnings("hiding")
        public Map<GK2, GV2> good = new LinkedHashMap<>();
    }

    public void testWithHiddenMap() {
        final SampleMaps2<String, Software, String, Software, ?, ?> sample = new SampleMaps2<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good = new LinkedHashMap<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Windows", new Software("Microsoft", "Windows"));
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Android", new Software("Google", "Android"));
        sample.good.put("iOS", new Software("Apple", "iOS"));
        sample.bad = null;

        final String expected = ""
            + "<sample2>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Google</vendor>\n"
            + "    <name>Android</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Apple</vendor>\n"
            + "    <name>iOS</name>\n"
            + "  </software>\n"
            + "</sample2>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps2.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithHiddenMapAndDifferentAlias() {
        final SampleMaps2<String, Software, String, Software, ?, ?> sample = new SampleMaps2<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good = new LinkedHashMap<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Windows", new Software("Microsoft", "Windows"));
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Android", new Software("Google", "Android"));
        sample.good.put("iOS", new Software("Apple", "iOS"));
        sample.bad = null;

        xstream.addImplicitMap(SampleMaps.class, "good", "code", Software.class, "name");
        xstream.addImplicitMap(SampleMaps2.class, "good", "mobile", Software.class, "name");
    }

    public void testDoesNotInheritFromHiddenMapOfSuperclass() {
        final SampleMaps2<String, Software, String, Software, ?, ?> sample = new SampleMaps2<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good = new LinkedHashMap<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Windows", new Software("Microsoft", "Windows"));
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Android", new Software("Google", "Android"));
        sample.good.put("iOS", new Software("Apple", "iOS"));
        sample.bad = null;

        final String expected = ""
            + "<sample2>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <good>\n"
            + "    <entry>\n"
            + "      <string>Android</string>\n"
            + "      <software>\n"
            + "        <vendor>Google</vendor>\n"
            + "        <name>Android</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "    <entry>\n"
            + "      <string>iOS</string>\n"
            + "      <software>\n"
            + "        <vendor>Apple</vendor>\n"
            + "        <name>iOS</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "  </good>\n"
            + "</sample2>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testDoesNotPropagateToHiddenMapOfSuperclass() {
        final SampleMaps2<String, Software, String, Software, ?, ?> sample = new SampleMaps2<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good = new LinkedHashMap<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Windows", new Software("Microsoft", "Windows"));
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Android", new Software("Google", "Android"));
        sample.good.put("iOS", new Software("Apple", "iOS"));
        sample.bad = null;

        final String expected = ""
            + "<sample2>\n"
            + "  <good defined-in=\"sample\">\n"
            + "    <entry>\n"
            + "      <string>Windows</string>\n"
            + "      <software>\n"
            + "        <vendor>Microsoft</vendor>\n"
            + "        <name>Windows</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "    <entry>\n"
            + "      <string>Linux</string>\n"
            + "      <software>\n"
            + "        <vendor>Red Hat</vendor>\n"
            + "        <name>Linux</name>\n"
            + "      </software>\n"
            + "    </entry>\n"
            + "  </good>\n"
            + "  <software>\n"
            + "    <vendor>Google</vendor>\n"
            + "    <name>Android</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Apple</vendor>\n"
            + "    <name>iOS</name>\n"
            + "  </software>\n"
            + "</sample2>";

        xstream.addImplicitMap(SampleMaps2.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public static class IntermediateMaps<GK2, GV2, GK, GV, BK, BV> extends SampleMaps2<GK2, GV2, GK, GV, BK, BV> {
        private static final long serialVersionUID = 201509L;
    }

    public static class SampleMaps3<GK3, GV3, GK2, GV2, GK, GV, BK, BV> extends
        IntermediateMaps<GK2, GV2, GK, GV, BK, BV> {
        private static final long serialVersionUID = 201509L;
        @SuppressWarnings("hiding")
        Map<GK3, GV3> good = new LinkedHashMap<>();
    }

    public void testWithDoubleHiddenList() {
        final SampleMaps3<String, Software, String, Software, String, Software, ?, ?> sample = new SampleMaps3<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good = new LinkedHashMap<>();
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Windows", new Software("Microsoft", "Windows"));
        ((SampleMaps<String, Software, ?, ?>)sample).good.put("Linux", new Software("Red Hat", "Linux"));
        ((SampleMaps2<String, Software, String, Software, ?, ?>)sample).good.put("Android", new Software("Google",
            "Android"));
        ((SampleMaps2<String, Software, String, Software, ?, ?>)sample).good.put("iOS", new Software("Apple", "iOS"));
        sample.good.put("Oracle", new Software("Oracle", "Oracle"));
        sample.good.put("Hana", new Software("SAP", "Hana"));
        sample.bad = null;

        final String expected = ""
            + "<sample3>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </software>\n"
            + "  <software defined-in=\"sample\">\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <software defined-in=\"sample2\">\n"
            + "    <vendor>Google</vendor>\n"
            + "    <name>Android</name>\n"
            + "  </software>\n"
            + "  <software defined-in=\"sample2\">\n"
            + "    <vendor>Apple</vendor>\n"
            + "    <name>iOS</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>Oracle</vendor>\n"
            + "    <name>Oracle</name>\n"
            + "  </software>\n"
            + "  <software>\n"
            + "    <vendor>SAP</vendor>\n"
            + "    <name>Hana</name>\n"
            + "  </software>\n"
            + "</sample3>";

        xstream.addImplicitMap(SampleMaps.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps2.class, "good", Software.class, "name");
        xstream.addImplicitMap(SampleMaps3.class, "good", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testCollectsDifferentTypesWithFieldOfSameName() {
        final SampleMaps<String, Object, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("iPhone", new Product("iPhone", "i", 399.99));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));
        sample.good.put("Intel", new Hardware("i386", "Intel"));

        final String expected = ""
            + "<sample>\n"
            + "  <product>\n"
            + "    <name>iPhone</name>\n"
            + "    <id>i</id>\n"
            + "    <price>399.99</price>\n"
            + "  </product>\n"
            + "  <software>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </software>\n"
            + "  <hardware>\n"
            + "    <arch>i386</arch>\n"
            + "    <name>Intel</name>\n"
            + "  </hardware>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", null, "name");
        assertBothWays(sample, expected);
    }

    public void testSeparatesItemsBasedOnItemName() {
        final SampleMaps<String, Software, String, Software> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("Chrome", new Software("Google", "Chrome"));
        sample.bad = new LinkedHashMap<>();
        sample.bad.put("Linux", new Software("Red Hat", "Linux"));
        sample.bad.put("Windows", new Software("Microsoft", "Windows"));

        final String expected = ""
            + "<sample>\n"
            + "  <g>\n"
            + "    <vendor>Google</vendor>\n"
            + "    <name>Chrome</name>\n"
            + "  </g>\n"
            + "  <b>\n"
            + "    <vendor>Red Hat</vendor>\n"
            + "    <name>Linux</name>\n"
            + "  </b>\n"
            + "  <b>\n"
            + "    <vendor>Microsoft</vendor>\n"
            + "    <name>Windows</name>\n"
            + "  </b>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", "g", Software.class, "name");
        xstream.addImplicitMap(SampleMaps.class, "bad", "b", Software.class, "name");
        assertBothWays(sample, expected);
    }

    public void testWithoutKeyField() {
        final SampleMaps<String, Software, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put("Windows", new Software("Microsoft", "Windows"));
        sample.good.put("Linux", new Software("Red Hat", "Linux"));

        final String expected = ""
            + "<sample>\n"
            + "  <entry>\n"
            + "    <string>Windows</string>\n"
            + "    <software>\n"
            + "      <vendor>Microsoft</vendor>\n"
            + "      <name>Windows</name>\n"
            + "    </software>\n"
            + "  </entry>\n"
            + "  <entry>\n"
            + "    <string>Linux</string>\n"
            + "    <software>\n"
            + "      <vendor>Red Hat</vendor>\n"
            + "      <name>Linux</name>\n"
            + "    </software>\n"
            + "  </entry>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", null, null);
        assertBothWays(sample, expected);
    }

    public void testCanUsePrimitiveAsKey() {
        final SampleMaps<Double, Product, ?, ?> sample = new SampleMaps<>();
        sample.good = new LinkedHashMap<>();
        sample.good.put(new Double(399.99), new Product("iPhone", "i", 399.99));

        final String expected = ""
            + "<sample>\n"
            + "  <product>\n"
            + "    <name>iPhone</name>\n"
            + "    <id>i</id>\n"
            + "    <price>399.99</price>\n"
            + "  </product>\n"
            + "  <bad/>\n"
            + "</sample>";

        xstream.addImplicitMap(SampleMaps.class, "good", Product.class, "price");
        assertBothWays(sample, expected);
    }
}
