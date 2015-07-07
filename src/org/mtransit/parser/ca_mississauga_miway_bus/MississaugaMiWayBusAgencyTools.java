package org.mtransit.parser.ca_mississauga_miway_bus;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.CleanUtils;
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
		System.out.printf("\nGenerating Mississauga MiWay bus data...\n");
		long start = System.currentTimeMillis();
		this.serviceIds = extractUsefulServiceIds(args, this);
		super.start(args);
		System.out.printf("\nGenerating Mississauga MiWay bus data... DONE in %s.\n", Utils.getPrettyDuration(System.currentTimeMillis() - start));
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
	public void setTripHeadsign(MRoute mRoute, MTrip mTrip, GTrip gTrip, GSpec gtfs) {
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
		mTrip.setHeadsignString(cleanTripHeadsign(gTrip.trip_headsign), gTrip.direction_id);
	}

	@Override
	public String cleanTripHeadsign(String tripHeadsign) {
		tripHeadsign = CleanUtils.cleanStreetTypes(tripHeadsign);
		return CleanUtils.cleanLabel(tripHeadsign);
	}

	private static final Pattern AT = Pattern.compile("( at )", Pattern.CASE_INSENSITIVE);
	private static final String AT_REPLACEMENT = " / ";

	private static final Pattern PLATFORM = Pattern.compile("( platform )", Pattern.CASE_INSENSITIVE);
	private static final String PLATFORM_REPLACEMENT = " P.";

	@Override
	public String cleanStopName(String gStopName) {
		gStopName = AT.matcher(gStopName).replaceAll(AT_REPLACEMENT);
		gStopName = PLATFORM.matcher(gStopName).replaceAll(PLATFORM_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}

	@Override
	public String getStopCode(GStop gStop) {
		return gStop.stop_id; // using stop ID as stop code
	}
}
