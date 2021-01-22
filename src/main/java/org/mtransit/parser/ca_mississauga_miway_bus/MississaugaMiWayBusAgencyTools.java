package org.mtransit.parser.ca_mississauga_miway_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.parser.CleanUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Utils;
import org.mtransit.parser.gtfs.data.GCalendar;
import org.mtransit.parser.gtfs.data.GCalendarDate;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;

import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://www.mississauga.ca/portal/miway/developerdownload
// https://www.miapp.ca/GTFS/google_transit.zip
public class MississaugaMiWayBusAgencyTools extends DefaultAgencyTools {

	public static void main(@Nullable String[] args) {
		if (args == null || args.length == 0) {
			args = new String[3];
			args[0] = "input/gtfs.zip";
			args[1] = "../../mtransitapps/ca-mississauga-miway-bus-android/res/raw/";
			args[2] = ""; // files-prefix
		}
		new MississaugaMiWayBusAgencyTools().start(args);
	}

	@Nullable
	private HashSet<Integer> serviceIdInts;

	@Override
	public void start(@NotNull String[] args) {
		MTLog.log("Generating Mississauga MiWay bus data...");
		long start = System.currentTimeMillis();
		this.serviceIdInts = extractUsefulServiceIdInts(args, this, true);
		super.start(args);
		MTLog.log("Generating Mississauga MiWay bus data... DONE in %s.", Utils.getPrettyDuration(System.currentTimeMillis() - start));
	}

	@Override
	public boolean excludingAll() {
		return this.serviceIdInts != null && this.serviceIdInts.isEmpty();
	}

	@Override
	public boolean excludeCalendar(@NotNull GCalendar gCalendar) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarInt(gCalendar, this.serviceIdInts);
		}
		return super.excludeCalendar(gCalendar);
	}

	@Override
	public boolean excludeCalendarDate(@NotNull GCalendarDate gCalendarDates) {
		if (this.serviceIdInts != null) {
			return excludeUselessCalendarDateInt(gCalendarDates, this.serviceIdInts);
		}
		return super.excludeCalendarDate(gCalendarDates);
	}

	@Override
	public boolean excludeTrip(@NotNull GTrip gTrip) {
		if (this.serviceIdInts != null) {
			return excludeUselessTripInt(gTrip, this.serviceIdInts);
		}
		return super.excludeTrip(gTrip);
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String AGENCY_COLOR = "D33517"; // ORANGE

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final String COLOR_6F5F5E = "6F5F5E";

	@Nullable
	@Override
	public String getRouteColor(@NotNull GRoute gRoute) {
		//noinspection deprecation
		int routeId = Integer.parseInt(gRoute.getRouteId());
		if (routeId >= 300 && routeId <= 399) { // School Routes
			return COLOR_6F5F5E;
		}
		return super.getRouteColor(gRoute);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		mTrip.setHeadsignString(
				cleanTripHeadsign(gTrip.getTripHeadsignOrDefault()),
				gTrip.getDirectionIdOrDefault()
		);
	}

	@Override
	public boolean directionFinderEnabled() {
		return true;
	}

	@Override
	public int getDirectionType() {
		return MTrip.HEADSIGN_TYPE_DIRECTION;
	}

	@NotNull
	@Override
	public String cleanDirectionHeadsign(boolean fromStopName, @NotNull String directionHeadSign) {
		directionHeadSign = cleanHeadSign(directionHeadSign);
		return directionHeadSign; // keep original head-sign bounds for convert to direction E/W/N/S
	}

	@Nullable
	@Override
	public MDirectionType convertDirection(@Nullable String headSign) {
		if (headSign != null) {
			if (getDirectionType() == MTrip.HEADSIGN_TYPE_DIRECTION) {
				final String tripHeadsignLC = headSign.toLowerCase(Locale.ENGLISH);
				switch (tripHeadsignLC) {
				case "eastbound":
					return MDirectionType.EAST;
				case "westbound":
					return MDirectionType.WEST;
				case "northbound":
					return MDirectionType.NORTH;
				case "southbound":
					return MDirectionType.SOUTH;
				case "cw":
					return null;
				default:
					throw new MTLog.Fatal("Unexpected direction for '%s'!", headSign);
				}
			}
		}
		return null;
	}

	private static final Pattern REMOVE_BOUNDS_ = CleanUtils.cleanWords("eastbound", "westbound", "northbound", "southbound");

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = REMOVE_BOUNDS_.matcher(tripHeadsign).replaceAll(EMPTY); // bounds used for direction
		return cleanHeadSign(tripHeadsign);
	}

	@NotNull
	private String cleanHeadSign(@NotNull String headSign) {
		headSign = CleanUtils.cleanStreetTypes(headSign);
		headSign = CleanUtils.cleanNumbers(headSign);
		return CleanUtils.cleanLabel(headSign);
	}

	private static final Pattern PLATFORM = Pattern.compile("( platform )", Pattern.CASE_INSENSITIVE);
	private static final String PLATFORM_REPLACEMENT = " P ";

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.cleanBounds(gStopName);
		gStopName = CleanUtils.CLEAN_AT.matcher(gStopName).replaceAll(CleanUtils.CLEAN_AT_REPLACEMENT);
		gStopName = PLATFORM.matcher(gStopName).replaceAll(PLATFORM_REPLACEMENT);
		gStopName = CleanUtils.cleanStreetTypes(gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		return CleanUtils.cleanLabel(gStopName);
	}
}
