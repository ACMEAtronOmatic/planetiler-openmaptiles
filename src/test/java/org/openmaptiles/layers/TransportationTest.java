package org.openmaptiles.layers;

import static com.onthegomap.planetiler.TestUtils.newLineString;
import static com.onthegomap.planetiler.TestUtils.newPoint;
import static com.onthegomap.planetiler.TestUtils.rectangle;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.onthegomap.planetiler.FeatureCollector;
import com.onthegomap.planetiler.config.Arguments;
import com.onthegomap.planetiler.config.PlanetilerConfig;
import com.onthegomap.planetiler.geo.GeometryException;
import com.onthegomap.planetiler.reader.SimpleFeature;
import com.onthegomap.planetiler.reader.SourceFeature;
import com.onthegomap.planetiler.reader.osm.OsmElement;
import com.onthegomap.planetiler.reader.osm.OsmRelationInfo;
import com.onthegomap.planetiler.stats.Stats;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmaptiles.OpenMapTilesProfile;

class TransportationTest extends AbstractLayerTest {

  @Test
  void testNamedFootway() {
    FeatureCollector result = process(lineFeature(Map.of(
      "name", "Lagoon Path",
      "surface", "asphalt",
      "level", "0",
      "highway", "footway",
      "indoor", "no",
      "oneway", "no",
      "foot", "designated",
      "bicycle", "dismount"
    )));
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "_type", "line",
      "class", "path",
      "subclass", "footway",
      "oneway", "<null>",
      "name", "<null>",
      "layer", "<null>",
      "_buffer", 4d,
      "_minpixelsize", 0d,
      "_minzoom", 13
    ), Map.of(
      "_layer", "transportation_name",
      "_type", "line",
      "class", "path",
      "subclass", "footway",
      "name", "Lagoon Path",
      "name_int", "Lagoon Path",
      "name:latin", "Lagoon Path",
      "_minpixelsize", 0d,
      "_minzoom", 13,
      "_maxzoom", 14
    )), result);
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "surface", "paved",
      "oneway", "<null>",
      "layer", "<null>",
      "level", 0L,
      "ramp", "<null>",
      "bicycle", "dismount",
      "foot", "designated"
    ), Map.of(
      "_layer", "transportation_name",
      "level", 0L,
      "surface", "<null>",
      "oneway", "<null>",
      "ramp", "<null>",
      "bicycle", "<null>",
      "foot", "<null>"
    )), result);
  }

  @Test
  void testImportantPath() {
    var rel = new OsmElement.Relation(1);

    rel.setTag("colour", "white");
    rel.setTag("name", "Appalachian Trail - 11 MA");
    rel.setTag("network", "nwn");
    rel.setTag("osmc", "symbol	white::white_stripe");
    rel.setTag("ref", "AT");
    rel.setTag("route", "hiking");
    rel.setTag("short_name", "AT 11 MA");
    rel.setTag("symbol", "white-paint blazes");
    rel.setTag("type", "route");
    rel.setTag("wikidata", "Q620648");
    rel.setTag("wikipedia", "en:Appalachian Trail");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "bicycle", "no",
        "highway", "path",
        "horse", "no",
        "name", "Appalachian Trail",
        "ref", "AT",
        "surface", "ground"
      )));
    assertFeatures(12, List.of(Map.of(
      "_layer", "transportation",
      "_type", "line",
      "class", "path",
      "subclass", "path",
      "oneway", "<null>",
      "name", "<null>",
      "layer", "<null>",
      "_buffer", 4d,
      "_minpixelsize", 0d,
      "_minzoom", 12
    ), Map.of(
      "_layer", "transportation_name",
      "_type", "line",
      "class", "path",
      "subclass", "path",
      "name", "Appalachian Trail",
      "name_int", "Appalachian Trail",
      "name:latin", "Appalachian Trail",
      "_minpixelsize", 0d,
      "_minzoom", 12
    )), features);
  }

  @Test
  void testUnnamedPath() {
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "subclass", "path",
      "surface", "unpaved",
      "oneway", "<null>",
      "_minzoom", 14
    )), process(lineFeature(Map.of(
      "surface", "dirt",
      "highway", "path"
    ))));
  }

  @Test
  void testPrivatePath() {
    assertFeatures(9, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "access", "no"
    )), process(lineFeature(Map.of(
      "access", "private",
      "highway", "path"
    ))));
    assertFeatures(9, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "access", "no"
    )), process(lineFeature(Map.of(
      "access", "no",
      "highway", "path"
    ))));
    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "access", "<null>"
    )), process(lineFeature(Map.of(
      "access", "no",
      "highway", "path"
    ))));
  }

  @Test
  void testExpressway() {
    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "expressway", "<null>"
    )), process(lineFeature(Map.of(
      "highway", "motorway",
      "expressway", "yes"
    ))));
    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "expressway", 1
    )), process(lineFeature(Map.of(
      "highway", "primary",
      "expressway", "yes"
    ))));
    assertFeatures(7, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "expressway", "<null>"
    )), process(lineFeature(Map.of(
      "highway", "primary",
      "expressway", "yes"
    ))));
  }

  @Test
  void testToll() {
    assertFeatures(9, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "toll", "<null>"
    )), process(lineFeature(Map.of(
      "highway", "motorway"
    ))));
    assertFeatures(9, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "toll", 1
    )), process(lineFeature(Map.of(
      "highway", "motorway",
      "toll", "yes"
    ))));
    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "toll", "<null>"
    )), process(lineFeature(Map.of(
      "highway", "motorway",
      "toll", "yes"
    ))));
  }

  @Test
  void testIndoorTunnelSteps() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "subclass", "steps",
      "brunnel", "tunnel",
      "indoor", 1,
      "oneway", 1,
      "ramp", "<null>"
    )), process(lineFeature(Map.of(
      "highway", "steps",
      "tunnel", "building_passage",
      "oneway", "yes",
      "indoor", "yes"
    ))));
  }

  @Test
  void testInterstateMotorway() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "US:I");
    rel.setTag("ref", "90");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "name", "Massachusetts Turnpike",
        "ref", "I 90",
        "surface", "asphalt",
        "foot", "no",
        "bicycle", "no",
        "horse", "no",
        "bridge", "yes"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "surface", "paved",
      "oneway", 1,
      "ramp", "<null>",
      "bicycle", "no",
      "foot", "no",
      "horse", "no",
      "brunnel", "bridge",
      "_minzoom", 3
    ), Map.ofEntries(
      entry("_layer", "transportation_name"),
      entry("class", "motorway"),
      entry("name", "Massachusetts Turnpike"),
      entry("name_en", "Massachusetts Turnpike"),
      entry("ref", "90"),
      entry("ref_length", 2),
      entry("network", "us-interstate"),
      entry("route_1_network", "US:I"),
      entry("route_1_ref", "90"),
      entry("brunnel", "<null>"),
      entry("_minzoom", 6)
    )), features);

    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "surface", "<null>",
      "oneway", "<null>",
      "ramp", "<null>",
      "bicycle", "<null>",
      "foot", "<null>",
      "horse", "<null>",
      "brunnel", "bridge",
      "_minzoom", 3
    ), Map.ofEntries(
      entry("_layer", "transportation_name"),
      entry("class", "motorway"),
      entry("name", "Massachusetts Turnpike"),
      entry("name_en", "Massachusetts Turnpike"),
      entry("ref", "90"),
      entry("ref_length", 2),
      entry("network", "us-interstate"),
      entry("route_1_network", "US:I"),
      entry("route_1_ref", "90"),
      entry("brunnel", "<null>"),
      entry("_minzoom", 6)
    )), features);
  }

  @Test
  void testDuplicateRoute() {
    var rel1 = new OsmElement.Relation(1);
    rel1.setTag("type", "route");
    rel1.setTag("route", "road");
    rel1.setTag("network", "US:OK");
    rel1.setTag("ref", "104");
    rel1.setTag("direction", "north");
    var rel2 = new OsmElement.Relation(1);
    rel2.setTag("type", "route");
    rel2.setTag("route", "road");
    rel2.setTag("network", "US:OK");
    rel2.setTag("ref", "104");
    rel2.setTag("direction", "south");

    FeatureCollector features = process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(rel2).stream(),
        profile.preprocessOsmRelation(rel1).stream()
      ).toList(),
      Map.of(
        "highway", "trunk",
        "ref", "US 23;SR 104",
        "lanes", 5,
        "maxspeed", "55 mph",
        "expressway", "no"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "trunk",
      "network", "us-state",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "104",
      "ref_length", 3,
      "network", "us-state",
      "route_1_network", "US:OK",
      "route_1_ref", "104",
      "route_2_network", "<null>",
      "route_2_ref", "<null>",
      "_minzoom", 8
    )), features);
  }

  @Test
  void testRouteWithoutNetworkType() {
    var rel1 = new OsmElement.Relation(1);
    rel1.setTag("type", "route");
    rel1.setTag("route", "road");
    rel1.setTag("network", "US:NJ:NJTP");
    rel1.setTag("ref", "NJTP");
    rel1.setTag("name", "New Jersey Turnpike (mainline)");

    var rel2 = new OsmElement.Relation(2);
    rel2.setTag("type", "route");
    rel2.setTag("route", "road");
    rel2.setTag("network", "US:I");
    rel2.setTag("ref", "95");
    rel2.setTag("name", "I 95 (NJ)");

    FeatureCollector rendered = process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(rel1).stream(),
        profile.preprocessOsmRelation(rel2).stream()
      ).toList(),
      Map.of(
        "highway", "motorway",
        "name", "New Jersey Turnpike",
        "ref", "I 95;NJTP"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "motorway",
      "_minzoom", 3
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "name", "New Jersey Turnpike",
      "ref", "95",
      "ref_length", 2,
      "route_1_network", "US:I",
      "route_1_ref", "95",
      "route_2_network", "US:NJ:NJTP",
      "route_2_ref", "NJTP",
      "_minzoom", 6
    )), rendered);
  }

  @Test
  void testRouteWithColour() {
    var rel1 = new OsmElement.Relation(1);
    rel1.setTag("type", "route");
    rel1.setTag("route", "road");
    rel1.setTag("network", "US:NJ:NJTP");
    rel1.setTag("ref", "NJTP");
    rel1.setTag("name", "New Jersey Turnpike (mainline)");
    rel1.setTag("colour", "#FFFFFF");
    rel1.setTag("ref:colour", "#888888");

    var rel2 = new OsmElement.Relation(2);
    rel2.setTag("type", "route");
    rel2.setTag("route", "road");
    rel2.setTag("network", "US:I");
    rel2.setTag("ref", "95");
    rel2.setTag("name", "I 95 (NJ)");
    rel2.setTag("ref:colour", "#000000");

    FeatureCollector rendered = process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(rel1).stream(),
        profile.preprocessOsmRelation(rel2).stream()
      ).toList(),
      Map.of(
        "highway", "motorway",
        "name", "New Jersey Turnpike",
        "ref", "I 95;NJTP"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "motorway",
      "_minzoom", 3
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "route_1_network", "US:I",
      "route_1_colour", "#000000",
      "route_2_network", "US:NJ:NJTP",
      "route_2_colour", "#FFFFFF",
      "_minzoom", 6
    )), rendered);
  }

  @Test
  void testSegmentWithManyRoutes() {
    List<OsmRelationInfo> relations = new ArrayList<>();
    for (int route = 1; route <= 16; route++) {
      int num = (route + 1) / 2; // to make dups
      var rel = new OsmElement.Relation(num);
      rel.setTag("type", "route");
      rel.setTag("route", "road");
      rel.setTag("network", "US:I");
      rel.setTag("ref", Integer.toString(num));
      rel.setTag("name", "Route " + num);
      relations.addAll(profile.preprocessOsmRelation(rel));
    }

    FeatureCollector rendered = process(lineFeatureWithRelation(
      relations,
      Map.of(
        "highway", "motorway",
        "name", "New Jersey Turnpike",
        "ref", "I 95;NJTP"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "motorway",
      "_minzoom", 3
    ), mapOf(
      "_layer", "transportation_name",
      "route_1_network", "US:I",
      "route_1_ref", "1",
      "route_2_network", "US:I",
      "route_2_ref", "2",
      "route_3_network", "US:I",
      "route_3_ref", "3",
      "route_4_network", "US:I",
      "route_4_ref", "4",
      "route_5_network", "US:I",
      "route_5_ref", "5",
      "route_6_network", "US:I",
      "route_6_ref", "6",
      "route_7_network", "US:I",
      "route_7_ref", "7",
      "route_8_network", "US:I",
      "route_8_ref", "8",
      "route_9_network", "<null>",
      "route_9_ref", "<null>"
    )), rendered);
  }

  @Test
  void testMinorRouteRef() {
    var rel1 = new OsmElement.Relation(1);
    rel1.setTag("type", "route");
    rel1.setTag("route", "road");
    rel1.setTag("network", "rwn");
    rel1.setTag("ref", "GFW");
    rel1.setTag("name", "Georg-Fahrbach-Weg");

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "tertiary"
    )), process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel1),
      Map.of(
        "highway", "tertiary"
      ))));

    var profileWithMinorRefs = new OpenMapTilesProfile(translations, PlanetilerConfig.from(Arguments.of(Map.of(
      "transportation_name_minor_refs", "true"
    ))), Stats.inMemory());

    SourceFeature feature = lineFeatureWithRelation(
      profileWithMinorRefs.preprocessOsmRelation(rel1),
      Map.of(
        "highway", "tertiary"
      ));
    var collector = featureCollectorFactory.get(feature);
    profileWithMinorRefs.processFeature(feature, collector);
    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "tertiary"
    ), mapOf(
      "_layer", "transportation_name",
      "class", "tertiary",
      "ref", "GFW",
      "network", "road"
    )), collector);
  }

  @Test
  void testPolishHighwayIssue165() {
    var rel1 = new OsmElement.Relation(1);
    rel1.setTag("type", "route");
    rel1.setTag("route", "road");
    rel1.setTag("network", "e-road");
    rel1.setTag("ref", "E 77");
    rel1.setTag("name", "European route E 77");

    var rel2 = new OsmElement.Relation(2);
    rel2.setTag("type", "route");
    rel2.setTag("route", "road");
    rel2.setTag("network", "e-road");
    rel2.setTag("ref", "E 28");
    rel2.setTag("name", "European route E 28");

    FeatureCollector rendered = process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(rel1).stream(),
        profile.preprocessOsmRelation(rel2).stream()
      ).toList(),
      Map.of(
        "highway", "trunk",
        "ref", "S7"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "trunk"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "name", "<null>",
      "ref", "S7",
      "ref_length", 2,
      "route_1_network", "e-road",
      "route_1_ref", "E 28",
      "route_2_network", "e-road",
      "route_2_ref", "E 77"
    )), rendered);
  }

  @Test
  void testMotorwayJunction() {
    var otherNode1 = new OsmElement.Node(1, 1, 1);
    var junctionNode = new OsmElement.Node(2, 1, 2);
    var otherNode2 = new OsmElement.Node(3, 1, 3);
    var otherNode3 = new OsmElement.Node(4, 2, 3);

    junctionNode.setTag("highway", "motorway_junction");
    junctionNode.setTag("name", "exit 1");
    junctionNode.setTag("layer", "1");
    junctionNode.setTag("ref", "12");

    // 2 ways meet at junctionNode (id=2) - use most important class of a highway intersecting it (motorway)
    var way1 = new OsmElement.Way(5);
    way1.setTag("highway", "motorway");
    way1.nodes().add(otherNode1.id(), junctionNode.id(), otherNode2.id());
    var way2 = new OsmElement.Way(6);
    way2.setTag("highway", "primary");
    way2.nodes().add(junctionNode.id(), otherNode3.id());

    profile.preprocessOsmNode(otherNode1);
    profile.preprocessOsmNode(junctionNode);
    profile.preprocessOsmNode(otherNode2);
    profile.preprocessOsmNode(otherNode3);

    profile.preprocessOsmWay(way1);
    profile.preprocessOsmWay(way2);

    FeatureCollector features = process(SimpleFeature.create(
      newPoint(1, 2),
      junctionNode.tags(),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      junctionNode.id()
    ));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation_name",
      "class", "motorway",
      "subclass", "junction",
      "name", "exit 1",
      "ref", "12",
      "ref_length", 2,
      "layer", 1L,
      "_type", "point",
      "_minzoom", 10
    )), features);
  }

  @Test
  void testInterstateMotorwayWithoutWayInfo() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "US:I");
    rel.setTag("ref", "90");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "motorway"
      )));

    assertFeatures(13, List.of(mapOf(
      "_layer", "transportation",
      "class", "motorway",
      "network", "us-interstate",
      "_minzoom", 3
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "90",
      "ref_length", 2,
      "network", "us-interstate",
      "brunnel", "<null>",
      "route_1_network", "US:I",
      "route_1_ref", "90",
      "_minzoom", 6
    )), features);
  }

  @Test
  void testMotorwayRoadConstruction() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway_construction",
      "oneway", 1,
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "name", "D4 II/118 – Milín",
      "class", "motorway_construction",
      "_minzoom", 6
    )), process(lineFeature(Map.of(
      "highway", "construction",
      "construction", "motorway",
      "name", "D4 II/118 – Milín",
      "oneway", "yes"
    ))));
  }

  @Test
  void testPrimaryRoadConstruction() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary_construction",
      "brunnel", "bridge",
      "layer", 1L,
      "oneway", 1,
      "_minzoom", 7
    ), Map.of(
      "_layer", "transportation_name",
      "name", "North Washington Street",
      "class", "primary_construction",
      "brunnel", "<null>",
      "_minzoom", 12
    )), process(lineFeature(Map.of(
      "highway", "construction",
      "construction", "primary",
      "bridge", "yes",
      "layer", "1",
      "name", "North Washington Street",
      "oneway", "yes"
    ))));
  }

  @Test
  void testBridgeConstruction() {
    assertFeatures(14, List.of(), process(lineFeature(Map.of(
      "highway", "construction",
      "construction", "bridge",
      "man_made", "bridge",
      "layer", "1"
    ))));
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "minor_construction",
      "brunnel", "bridge",
      "layer", 1L
    )), process(closedWayFeature(Map.of(
      "highway", "construction",
      "construction", "bridge",
      "man_made", "bridge",
      "layer", "1"
    ))));
  }

  @Test
  void testIgnoreManMadeWhenNotBridgeOrPier() {
    // https://github.com/onthegomap/planetiler/issues/69
    assertFeatures(14, List.of(), process(lineFeature(Map.of(
      "man_made", "storage_tank",
      "service", "driveway"
    ))));
    assertFeatures(14, List.of(), process(lineFeature(Map.of(
      "man_made", "courtyard",
      "service", "driveway"
    ))));
    assertFeatures(14, List.of(), process(lineFeature(Map.of(
      "man_made", "courtyard",
      "service", "driveway",
      "name", "Named Driveway"
    ))));
  }

  @Test
  void testRaceway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "raceway",
      "oneway", 1,
      "_minzoom", 12
    ), Map.of(
      "_layer", "transportation_name",
      "class", "raceway",
      "name", "Climbing Turn",
      "ref", "5",
      "_minzoom", 12
    )), process(lineFeature(Map.of(
      "highway", "raceway",
      "oneway", "yes",
      "ref", "5",
      "name", "Climbing Turn"
    ))));
  }

  @Test
  void testDriveway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "service",
      "service", "driveway",
      "_minzoom", 14
    )), process(lineFeature(Map.of(
      "highway", "service",
      "service", "driveway"
    ))));
  }

  @Test
  void testMountainBikeTrail() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "path",
      "subclass", "path",
      "mtb_scale", "4",
      "surface", "unpaved",
      "bicycle", "yes",
      "_minzoom", 13
    ), Map.of(
      "_layer", "transportation_name",
      "class", "path",
      "subclass", "path",
      "name", "Path name",
      "_minzoom", 13
    )), process(lineFeature(Map.of(
      "highway", "path",
      "mtb:scale", "4",
      "name", "Path name",
      "bicycle", "yes",
      "surface", "ground"
    ))));
  }

  @Test
  void testNamedTrack() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "track",
      "surface", "unpaved",
      "horse", "yes",
      "_minzoom", 13
    ), Map.of(
      "_layer", "transportation_name",
      "class", "track",
      "name", "name",
      "_minzoom", 13
    )), process(lineFeature(Map.of(
      "highway", "track",
      "surface", "dirt",
      "horse", "yes",
      "name", "name"
    ))));
  }

  @Test
  void testUnnamedTrack() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "track",
      "surface", "unpaved",
      "horse", "yes",
      "_minzoom", 14
    )), process(lineFeature(Map.of(
      "highway", "track",
      "surface", "dirt",
      "horse", "yes"
    ))));
  }

  @Test
  void testBusway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "busway",
      "brunnel", "tunnel",
      "_minzoom", 11
    ), Map.of(
      "_layer", "transportation_name",
      "class", "busway",
      "name", "Silver Line",
      "_minzoom", 12
    )), process(lineFeature(Map.of(
      "access", "no",
      "bus", "yes",
      "highway", "busway",
      "layer", "-1",
      "name", "Silver Line",
      "trolley_wire", "yes",
      "tunnel", "yes"
    ))));
  }

  @Test
  void testBusGuideway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "bus_guideway",
      "brunnel", "tunnel",
      "_minzoom", 11
    ), Map.of(
      "_layer", "transportation_name",
      "class", "bus_guideway",
      "name", "Silver Line",
      "_minzoom", 12
    )), process(lineFeature(Map.of(
      "access", "no",
      "bus", "yes",
      "highway", "bus_guideway",
      "layer", "-1",
      "name", "Silver Line",
      "trolley_wire", "yes",
      "tunnel", "yes"
    ))));
  }

  final OsmElement.Relation relUS = new OsmElement.Relation(1);

  {
    relUS.setTag("type", "route");
    relUS.setTag("route", "road");
    relUS.setTag("network", "US:US");
    relUS.setTag("ref", "3");
  }

  final OsmElement.Relation relMA = new OsmElement.Relation(2);

  {
    relMA.setTag("type", "route");
    relMA.setTag("route", "road");
    relMA.setTag("network", "US:MA");
    relMA.setTag("ref", "2");
  }

  @Test
  void testUSAndStateHighway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "surface", "paved",
      "oneway", "<null>",
      "ramp", "<null>",
      "network", "us-highway",
      "_minzoom", 7
    ), Map.ofEntries(
      entry("_layer", "transportation_name"),
      entry("class", "primary"),
      entry("name", "Memorial Drive"),
      entry("name_en", "Memorial Drive"),
      entry("ref", "3"),
      entry("ref_length", 1),
      entry("network", "us-highway"),
      entry("route_1_network", "US:US"),
      entry("route_1_ref", "3"),
      entry("route_2_network", "US:MA"),
      entry("route_2_ref", "2"),
      entry("_minzoom", 12)
    )), process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(relUS).stream(),
        profile.preprocessOsmRelation(relMA).stream()
      ).toList(),
      Map.of(
        "highway", "primary",
        "name", "Memorial Drive",
        "ref", "US 3;MA 2",
        "surface", "asphalt"
      ))));

    // swap order
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "primary",
      "route_1_network", "US:US",
      "route_1_ref", "3",
      "route_2_network", "US:MA",
      "route_2_ref", "2",
      "ref", "3",
      "network", "us-highway"
    )), process(lineFeatureWithRelation(
      Stream.concat(
        profile.preprocessOsmRelation(relMA).stream(),
        Stream.concat( // ignore duplicates
          profile.preprocessOsmRelation(relUS).stream(),
          profile.preprocessOsmRelation(relUS).stream()
        )
      ).toList(),
      Map.of(
        "highway", "primary",
        "name", "Memorial Drive",
        "ref", "US 3;MA 2",
        "surface", "asphalt"
      ))));
  }

  @Test
  void testUsStateHighway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "primary",
      "name", "Memorial Drive",
      "name_en", "Memorial Drive",
      "ref", "2",
      "ref_length", 1,
      "network", "us-state",
      "_minzoom", 12
    )), process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(relMA),
      Map.of(
        "highway", "primary",
        "name", "Memorial Drive",
        "ref", "US 3;MA 2",
        "surface", "asphalt"
      ))));
  }

  @Test
  void testCompoundRef() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "network", "<null>"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "primary",
      "name", "Memorial Drive",
      "name_en", "Memorial Drive",
      "ref", "US 3;MA 2",
      "ref_length", 9,
      "network", "road",
      "_minzoom", 12
    )), process(lineFeature(
      Map.of(
        "highway", "primary",
        "name", "Memorial Drive",
        "ref", "US 3;MA 2",
        "surface", "asphalt"
      ))));
  }

  @Test
  void testTransCanadaHighway() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:transcanada:namedRoute");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "name", "Autoroute Claude-Béchard",
        "ref", "85",
        "surface", "asphalt"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "surface", "paved",
      "oneway", 1,
      "ramp", "<null>",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "name", "Autoroute Claude-Béchard",
      "name_en", "Autoroute Claude-Béchard",
      "ref", "85",
      "ref_length", 2,
      "network", "ca-transcanada",
      "_minzoom", 6
    )), features);
  }

  @Test
  void testTransCanadaTrunk() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:transcanada:namedRoute");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "_minzoom", 4
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaQcA() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:QC:A");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaOnPrimaryRef4xx() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:ON:primary");
    rel.setTag("ref", "420");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "420",
      "network", "ca-provincial-arterial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaOnPrimaryRefQew() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:ON:primary");
    rel.setTag("ref", "QEW");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "QEW",
      "network", "ca-provincial-arterial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaOnPrimaryRefOther() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:ON:primary");
    rel.setTag("ref", "85");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "85",
      "network", "ca-provincial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaMbPthRef75() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:MB:PTH");
    rel.setTag("ref", "75");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "75",
      "network", "ca-provincial-arterial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaMbPthRefOther() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:MB:PTH");
    rel.setTag("ref", "77");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "77",
      "network", "ca-provincial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaAbPrimaryRef3() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:AB:primary");
    rel.setTag("ref", "3");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "3",
      "network", "ca-provincial-arterial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaAbPrimaryRefOther() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:AB:primary");
    rel.setTag("ref", "10");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "10",
      "network", "ca-provincial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaBcRef3() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:BC");
    rel.setTag("ref", "3");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial-arterial",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "3",
      "network", "ca-provincial-arterial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaBcRefOther() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:BC");
    rel.setTag("ref", "10");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "network", "ca-provincial",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "10",
      "network", "ca-provincial"
    )), features);
  }

  @Test
  void testTransCanadaProvincialCaOther() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "CA:yellowhead");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "_minzoom", 6
    )), features);
    boolean caProvPresent = StreamSupport.stream(features.spliterator(), false)
      .flatMap(f -> f.getAttrsAtZoom(13).entrySet().stream())
      .filter(e -> "network".equals(e.getKey()))
      .map(Map.Entry::getValue)
      .anyMatch(v -> "ca-provincial".equals(v) || "ca-provincial-arterial".equals(v));
    assertFalse(caProvPresent, "ca-provincial present");
  }

  @Test
  void testGreatBritainHighway() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "GB"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "oneway", 1,
      "ramp", "<null>",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "M1",
      "ref_length", 2,
      "network", "gb-motorway",
      "_minzoom", 6
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "ref", "M1"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));

    // not in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "oneway", 1,
      "ramp", "<null>",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "M1",
      "ref_length", 2,
      "network", "road",
      "_minzoom", 6
    )), process(SimpleFeature.create(
      newLineString(1, 0, 0, 1),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "ref", "M1"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testGreatBritainTrunk() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "GB"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "_minzoom", 5
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "A272",
      "ref_length", 4,
      "network", "gb-trunk",
      "_minzoom", 8
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "trunk",
        "ref", "A272"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testGreatBritainPrimary() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "GB"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "_minzoom", 7
    ), Map.of(
      "_layer", "transportation_name",
      "class", "primary",
      "ref", "A598",
      "ref_length", 4,
      "network", "gb-primary",
      "_minzoom", 12
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "primary",
        "ref", "A598"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testGreatBritainSecondary() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "GB"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "secondary",
      "_minzoom", 9
    ), Map.of(
      "_layer", "transportation_name",
      "class", "secondary",
      "ref", "B4558",
      "ref_length", 5,
      "network", "gb-primary",
      "_minzoom", 12
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "secondary",
        "ref", "B4558"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testGreatBritainTertiary() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "GB"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in GB
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "tertiary",
      "_minzoom", 11
    ), Map.of(
      "_layer", "transportation_name",
      "class", "tertiary",
      "ref", "B4086",
      "ref_length", 5,
      "network", "road",
      "_minzoom", 12
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "tertiary",
        "ref", "B4086"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testIrelandHighway() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "IE"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in IE
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "oneway", 1,
      "ramp", "<null>",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "M18",
      "ref_length", 3,
      "network", "ie-motorway",
      "_minzoom", 6
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "ref", "M18"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));

    // not in IE
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "oneway", 1,
      "ramp", "<null>",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "M18",
      "ref_length", 3,
      "network", "road",
      "_minzoom", 6
    )), process(SimpleFeature.create(
      newLineString(1, 0, 0, 1),
      Map.of(
        "highway", "motorway",
        "oneway", "yes",
        "ref", "M18"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testIrelandTrunk() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "IE"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in IE
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "N8",
      "ref_length", 2,
      "network", "ie-national",
      "_minzoom", 8
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "trunk",
        "ref", "N8"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testIrelandPrimary() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "IE"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in IE
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "primary",
      "_minzoom", 7
    ), Map.of(
      "_layer", "transportation_name",
      "class", "primary",
      "ref", "N59",
      "ref_length", 3,
      "network", "ie-national",
      "_minzoom", 12
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "primary",
        "ref", "N59"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testIrelandSecondary() {
    process(SimpleFeature.create(
      rectangle(0, 0.1),
      Map.of("iso_a2", "IE"),
      OpenMapTilesProfile.NATURAL_EARTH_SOURCE,
      "ne_10m_admin_0_countries",
      0
    ));

    // in IE
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "secondary",
      "_minzoom", 9
    ), Map.of(
      "_layer", "transportation_name",
      "class", "secondary",
      "ref", "R813",
      "ref_length", 4,
      "network", "ie-regional",
      "_minzoom", 12
    )), process(SimpleFeature.create(
      newLineString(0, 0, 1, 1),
      Map.of(
        "highway", "secondary",
        "ref", "R813"
      ),
      OpenMapTilesProfile.OSM_SOURCE,
      null,
      0
    )));
  }

  @Test
  void testMergesDisconnectedRoadNameFeatures() throws GeometryException {
    testMergesLinestrings(Map.of("class", "motorway"), TransportationName.LAYER_NAME, 10, 14);
  }

  @Test
  void testMergesDisconnectedRoadFeaturesUnlessOneway() throws GeometryException {
    String layer = Transportation.LAYER_NAME;
    testMergesLinestrings(Map.of("class", "motorway", "oneway", 0), layer, 10, 14);
    testMergesLinestrings(Map.of("class", "motorway"), layer, 10, 14);
    testDoesNotMergeLinestrings(Map.of("class", "motorway", "oneway", 1), layer, 10, 14);
    testDoesNotMergeLinestrings(Map.of("class", "motorway", "oneway", -1), layer, 10, 14);
  }

  @Test
  void testMergesDisconnectedRoadFeaturesUnlessOnewayLong() throws GeometryException {
    String layer = Transportation.LAYER_NAME;
    testMergesLinestrings(Map.of("class", "motorway", "oneway", 0L), layer, 10, 14);
    testMergesLinestrings(Map.of("class", "motorway"), layer, 10, 14);
    testDoesNotMergeLinestrings(Map.of("class", "motorway", "oneway", 1L), layer, 10, 14);
    testDoesNotMergeLinestrings(Map.of("class", "motorway", "oneway", -1L), layer, 10, 14);
  }

  @Test
  void testLightRail() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "transit",
      "subclass", "light_rail",
      "brunnel", "tunnel",
      "layer", -1L,
      "oneway", "<null>",
      "ramp", "<null>",

      "_minzoom", 11,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "railway", "light_rail",
      "name", "Green Line",
      "tunnel", "yes",
      "layer", "-1"
    ))));
  }

  @Test
  void testSubway() {
    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "transit",
      "subclass", "subway",
      "brunnel", "tunnel",
      "layer", -2L,
      "oneway", "<null>",
      "ramp", "<null>",

      "_minzoom", 14,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "railway", "subway",
      "name", "Red Line",
      "tunnel", "yes",
      "layer", "-2",
      "level", "-2"
    ))));
  }

  @Test
  void testRail() {
    assertFeatures(8, List.of(Map.of(
      "_layer", "transportation",
      "class", "rail",
      "subclass", "rail",
      "brunnel", "<null>",
      "layer", "<null>",

      "_minzoom", 8,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "railway", "rail",
      "name", "Boston Subdivision",
      "usage", "main",
      "tunnel", "yes",
      "layer", "-2"
    ))));
    assertFeatures(13, List.of(Map.of(
      "layer", "<null>",
      "_minzoom", 10
    )), process(lineFeature(Map.of(
      "railway", "rail",
      "name", "Boston Subdivision"
    ))));
    assertFeatures(13, List.of(),
      process(polygonFeature(Map.of(
        "railway", "rail"
      ))));
    assertFeatures(13, List.of(Map.of(
      "class", "rail",
      "subclass", "rail",
      "_minzoom", 14,
      "service", "yard"
    )), process(lineFeature(Map.of(
      "railway", "rail",
      "name", "Boston Subdivision",
      "service", "yard"
    ))));
  }

  @Test
  void testNarrowGauge() {
    assertFeatures(10, List.of(Map.of(
      "_layer", "transportation",
      "class", "rail",
      "subclass", "narrow_gauge",

      "_minzoom", 10,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "railway", "narrow_gauge"
    ))));
  }

  @ParameterizedTest
  @ValueSource(strings = {"gondola", "chair_lift", "j-bar", "mixed_lift"})
  void testAerialway(String aerialway) {
    assertFeatures(12, List.of(Map.of(
      "_layer", "transportation",
      "class", "aerialway",
      "subclass", aerialway,

      "_minzoom", 12,
      "_maxzoom", 14,
      "_type", "line"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "aerialway",
      "subclass", aerialway,
      "name", "Summit Gondola",

      "_minzoom", 12,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "aerialway", aerialway,
      "name", "Summit Gondola"
    ))));
    assertFeatures(10, List.of(),
      process(polygonFeature(Map.of(
        "aerialway", aerialway,
        "name", "Summit Gondola"
      ))));
  }

  @Test
  void testFerry() {
    assertFeatures(10, List.of(Map.of(
      "_layer", "transportation",
      "class", "ferry",

      "_minzoom", 4,
      "_maxzoom", 14,
      "_minpixelsize", 32d,
      "_type", "line"
    ), Map.of(
      "_layer", "transportation_name",
      "class", "ferry",
      "name", "Boston - Provincetown Ferry",

      "_minzoom", 12,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "route", "ferry",
      "name", "Boston - Provincetown Ferry",
      "motor_vehicle", "no",
      "foot", "yes",
      "bicycle", "yes"
    ))));
    assertFeatures(10, List.of(),
      process(polygonFeature(Map.of(
        "route", "ferry",
        "name", "Boston - Provincetown Ferry",
        "motor_vehicle", "no",
        "foot", "yes",
        "bicycle", "yes"
      ))));
  }

  @Test
  void testPiers() {
    // area
    assertFeatures(10, List.of(Map.of(
      "_layer", "transportation",
      "class", "pier",

      "_minzoom", 13,
      "_maxzoom", 14,
      "_type", "polygon"
    )), process(polygonFeature(Map.of(
      "man_made", "pier"
    ))));
    assertFeatures(10, List.of(Map.of(
      "_layer", "transportation",
      "class", "pier",

      "_minzoom", 13,
      "_maxzoom", 14,
      "_type", "line"
    )), process(lineFeature(Map.of(
      "man_made", "pier"
    ))));
  }

  @Test
  void testPedestrianArea() {
    Map<String, Object> pedestrianArea = Map.of(
      "_layer", "transportation",
      "class", "path",
      "subclass", "pedestrian",

      "_minzoom", 13,
      "_maxzoom", 14,
      "_type", "polygon"
    );
    Map<String, Object> circularPath = Map.of(
      "_layer", "transportation",
      "class", "path",
      "subclass", "pedestrian",

      "_minzoom", 14,
      "_maxzoom", 14,
      "_type", "line"
    );
    assertFeatures(14, List.of(pedestrianArea), process(closedWayFeature(Map.of(
      "highway", "pedestrian",
      "area", "yes",
      "foot", "yes"
    ))));
    assertFeatures(14, List.of(pedestrianArea), process(polygonFeature(Map.of(
      "highway", "pedestrian",
      "foot", "yes"
    ))));
    assertFeatures(14, List.of(circularPath), process(closedWayFeature(Map.of(
      "highway", "pedestrian",
      "foot", "yes"
    ))));
    assertFeatures(14, List.of(circularPath), process(closedWayFeature(Map.of(
      "highway", "pedestrian",
      "foot", "yes",
      "area", "no"
    ))));
    // ignore underground pedestrian areas
    assertFeatures(14, List.of(),
      process(polygonFeature(Map.of(
        "highway", "pedestrian",
        "area", "yes",
        "foot", "yes",
        "layer", "-1"
      ))));
  }

  private int getWaySortKey(Map<String, Object> tags) {
    var iter = process(lineFeature(tags)).iterator();
    return iter.next().getSortKey();
  }

  @Test
  void testSortKeys() {
    assertDescending(
      getWaySortKey(Map.of("highway", "footway", "layer", "2")),
      getWaySortKey(Map.of("highway", "motorway", "bridge", "yes")),
      getWaySortKey(Map.of("highway", "footway", "bridge", "yes")),
      getWaySortKey(Map.of("highway", "motorway")),
      getWaySortKey(Map.of("highway", "trunk")),
      getWaySortKey(Map.of("railway", "rail")),
      getWaySortKey(Map.of("highway", "primary")),
      getWaySortKey(Map.of("highway", "secondary")),
      getWaySortKey(Map.of("highway", "tertiary")),
      getWaySortKey(Map.of("highway", "motorway_link")),
      getWaySortKey(Map.of("highway", "footway")),
      getWaySortKey(Map.of("highway", "motorway", "tunnel", "yes")),
      getWaySortKey(Map.of("highway", "footway", "tunnel", "yes")),
      getWaySortKey(Map.of("highway", "motorway", "layer", "-2"))
    );
  }

  @Test
  void testTransportationNameLayerRequiresTransportationLayer() {
    var profile = new OpenMapTilesProfile(translations, PlanetilerConfig.from(Arguments.of(
      "only_layers", "transportation_name"
    )), Stats.inMemory());
    SourceFeature feature = lineFeature(Map.of(
      "highway", "path",
      "name", "test"
    ));
    var collector = featureCollectorFactory.get(feature);
    profile.processFeature(feature, collector);
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation_name",
      "class", "path",
      "name", "test"
    ), Map.of(
      "_layer", "transportation",
      "class", "path"
    )), collector);
  }

  @Test
  void testIssue86() {
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "bridge",
      "_minzoom", 13,
      "_type", "polygon"
    )), process(closedWayFeature(Map.of(
      "layer", "1",
      "man_made", "bridge",
      "service", "driveway"
    ))));
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "bridge",
      "_minzoom", 13,
      "_type", "polygon"
    )), process(closedWayFeature(Map.of(
      "layer", "1",
      "man_made", "bridge",
      "service", "driveway",
      "name", "name"
    ))));
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "pier",
      "_minzoom", 13,
      "_type", "polygon"
    )), process(closedWayFeature(Map.of(
      "layer", "1",
      "man_made", "pier",
      "service", "driveway"
    ))));
  }

  @Test
  void testGrade1SurfacePath() {
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "track",
      "surface", "paved"
    )), process(lineFeature(Map.of(
      "surface", "grade1",
      "highway", "track"
    ))));
  }

  @Test
  void testGrade1TracktypePath() {
    assertFeatures(14, List.of(Map.of(
      "_layer", "transportation",
      "class", "track",
      "surface", "paved"
    )), process(lineFeature(Map.of(
      "tracktype", "grade1",
      "highway", "track"
    ))));
  }

  @Test
  void testIssue58() {
    // test subject: https://www.openstreetmap.org/way/222564359
    // note: "name:es" used instead of "name:ar" since we've setup only "de" and "es" for unit tests
    FeatureCollector result = process(lineFeature(Map.of(
      "name", "איילון דרום",
      "name:es", "أيالون جنوب",
      "name:en", "Ayalon South",
      "highway", "motorway"
    )));
    assertFeatures(4, List.of(Map.of(
      "_layer", "transportation",
      "_type", "line",
      "class", "motorway"
    ), Map.of(
      "_layer", "transportation_name",
      "_type", "line",
      "class", "motorway",
      "name", "איילון דרום",
      "name_int", "Ayalon South",
      "name:latin", "Ayalon South",
      "name:es", "أيالون جنوب",
      "name:en", "Ayalon South"
    )), result);
  }

  @Test
  void testARoad() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "AsianHighway");
    rel.setTag("ref", "AH11");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "trunk",
        "name", "National Highway 7",
        "ref", "7"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "trunk",
      "_minzoom", 6
    ), Map.of(
      "_layer", "transportation_name",
      "class", "trunk",
      "ref", "7",
      "route_1_ref", "AH11"
    )), features);
  }

  @Test
  void testERoad() {
    var rel = new OsmElement.Relation(1);
    rel.setTag("type", "route");
    rel.setTag("route", "road");
    rel.setTag("network", "e-road");
    rel.setTag("ref", "E 77");

    FeatureCollector features = process(lineFeatureWithRelation(
      profile.preprocessOsmRelation(rel),
      Map.of(
        "highway", "motorway",
        "ref", "S7"
      )));

    assertFeatures(13, List.of(Map.of(
      "_layer", "transportation",
      "class", "motorway",
      "_minzoom", 4
    ), Map.of(
      "_layer", "transportation_name",
      "class", "motorway",
      "ref", "S7",
      "route_1_ref", "E 77"
    )), features);
  }
}
