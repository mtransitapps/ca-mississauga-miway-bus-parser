package org.mtransit.parser.ca_mississauga_miway_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MSpec;
import org.mtransit.parser.mt.data.MTrip;

// http://www.mississauga.ca/portal/miway/developerdownload
// https://www.miapp.ca/GTFS/google_transit.zip
public class MississaugaMiWayBusAgencyTools extends DefaultAgencyTools {

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-mississauga-miway-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new MississaugaMiWayBusAgencyTools().start(args);
	}

	private HashSet<String> serviceIds;

	@Override
	public void start(String[] args) {
		System.out.printf("Generating Mississauga MiWay bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("Generating Mississauga MiWay bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludeCalendar(GCalendar gCalendar) {
		if (this.serviceIds != null) {
			return excludeUselessCalendar(gCalendar, this.serviceIds);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(GCalendarDate gCalendarDates) {
		if (this.serviceIds != null) {
			return excludeUselessCalendarDate(gCalendarDates, this.serviceIds);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(GTrip gTrip) {
		if (this.serviceIds != null) {
			return excludeUselessTrip(gTrip, this.serviceIds);
		}
		return super.excludeTrip(gTrip);
	}

	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String AGENCY_COLOR = "D33517"; // ORANGE

	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_6F5F5E = "6F5F5E";

	@Override
	public String getRouteColor(GRoute gRoute) {
		int routeId = Integer.parseInt(gRoute.route_id);
		if (routeId >= 300 && routeId <= 399) { // School Routes
			return COLOR_6F5F5E;
		}
		return super.getRouteColor(gRoute);
	}

	private static final String WESTBOUND = "westbound";
	private static final String WEST = "west";
	private static final String EASTBOUND = "eastbound";
	private static final String EAST = "east";
	private static final String SOUTHBOUND = "southbound";
	private static final String SOUTH = "south";
	private static final String NORTHBOUND = "northbound";
	private static final String NORTH = "north";

	@Override
	public void setTripHeadsign(MRoute route, MTrip mTrip, GTrip gTrip) {
		String gTripHeadsignLC = gTrip.trip_headsign.toLowerCase(Locale.ENGLISH);
		if (gTripHeadsignLC.endsWith(NORTH) || gTripHeadsignLC.endsWith(NORTHBOUND)) {
			mTrip.setHeadsignDirection(MDirectionType.NORTH);
			return;
		} else if (gTripHeadsignLC.endsWith(SOUTH) || gTripHeadsignLC.endsWith(SOUTHBOUND)) {
			mTrip.setHeadsignDirection(MDirectionType.SOUTH);
			return;
		} else if (gTripHeadsignLC.endsWith(EAST) || gTripHeadsignLC.endsWith(EASTBOUND)) {
			mTrip.setHeadsignDirection(MDirectionType.EAST);
			return;
		} else if (gTripHeadsignLC.endsWith(WEST) || gTripHeadsignLC.endsWith(WESTBOUND)) {
			mTrip.setHeadsignDirection(MDirectionType.WEST);
			return;
		}
		int directionId = gTrip.direction_id;
		String stationName = cleanTripHeadsign(gTrip.trip_headsign);
		mTrip.setHeadsignString(stationName, directionId);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		return MSpec.cleanLabel(tripHeadsign);
	}

	private static final Pattern FIRST = Pattern.compile("(first )", Pattern.CASE_INSENSITIVE);
	private static final String FIRST_REPLACEMENT = "1st ";
	private static final Pattern SECOND = Pattern.compile("(second )", Pattern.CASE_INSENSITIVE);
	private static final String SECOND_REPLACEMENT = "2nd ";
	private static final Pattern THIRD = Pattern.compile("(third )", Pattern.CASE_INSENSITIVE);
	private static final String THIRD_REPLACEMENT = "3rd ";
	private static final Pattern FOURTH = Pattern.compile("(fourth )", Pattern.CASE_INSENSITIVE);
	private static final String FOURTH_REPLACEMENT = "4th";
	private static final Pattern FIFTH = Pattern.compile("(fifth )", Pattern.CASE_INSENSITIVE);
	private static final String FIFTH_REPLACEMENT = "5th ";
	private static final Pattern SIXTH = Pattern.compile("(sixth )", Pattern.CASE_INSENSITIVE);
	private static final String SIXTH_REPLACEMENT = "6th ";
	private static final Pattern SEVENTH = Pattern.compile("(seventh )", Pattern.CASE_INSENSITIVE);
	private static final String SEVENTH_REPLACEMENT = "7th ";
	private static final Pattern EIGHTH = Pattern.compile("(eighth )", Pattern.CASE_INSENSITIVE);
	private static final String EIGHTH_REPLACEMENT = "8th ";
	private static final Pattern NINTH = Pattern.compile("(ninth )", Pattern.CASE_INSENSITIVE);
	private static final String NINTH_REPLACEMENT = "9th ";

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = FIRST.matcher(gStopName).replaceAll(FIRST_REPLACEMENT);
		gStopName = SECOND.matcher(gStopName).replaceAll(SECOND_REPLACEMENT);
		gStopName = THIRD.matcher(gStopName).replaceAll(THIRD_REPLACEMENT);
		gStopName = FOURTH.matcher(gStopName).replaceAll(FOURTH_REPLACEMENT);
		gStopName = FIFTH.matcher(gStopName).replaceAll(FIFTH_REPLACEMENT);
		gStopName = SIXTH.matcher(gStopName).replaceAll(SIXTH_REPLACEMENT);
		gStopName = SEVENTH.matcher(gStopName).replaceAll(SEVENTH_REPLACEMENT);
		gStopName = EIGHTH.matcher(gStopName).replaceAll(EIGHTH_REPLACEMENT);
		gStopName = NINTH.matcher(gStopName).replaceAll(NINTH_REPLACEMENT);
		return MSpec.cleanLabel(gStopName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return gStop.stop_id; // using stop ID as stop code
	}
}
