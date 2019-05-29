package com.tavisca.myplaces;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.CompleteVenue;
import fi.foyt.foursquare.api.entities.Location;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;

@Controller
@RequestMapping(value = "/venues", produces = "application/json")
public class VenueController {
	private static final Logger logger = LoggerFactory.getLogger(VenueController.class);
	private static FoursquareApi foursquareApi = authenticateFourSquareApi();

	public static FoursquareApi authenticateFourSquareApi() {
		FoursquareApi foursquareApi = new FoursquareApi("4JDKZJAKTXB0OU2E2NNVNDL51OP32JG3J4GLN3XPUST0R4FP",
				"Y3OQGGHWX4WI0E2DYD2LHUXKUMKBCNS2AO0V0NQNXQI4IEMQ", "http://localhost:8080/MyPlaces/");
		return foursquareApi;
	}

	// API to search location by latitude and longitude
	@ResponseBody
	@RequestMapping(value = "/search", method = RequestMethod.GET, headers = "content-type=application/json", produces = "application/json")
	public List<CompactVenue> getVenues(Locale locale, @RequestBody Location location) {
		List<CompactVenue> venues = null;
		try {
			String ll = location.getLat() + "," + location.getLng();
			Result<VenuesSearchResult> result = foursquareApi.venuesSearch(ll, null, null, null, null, null, null, null,
					null, null, null, null, null);

			if (result.getMeta().getCode() == 200) {
				venues = new ArrayList<CompactVenue>();
				// if query was ok we can finally we do something with the data
				for (CompactVenue venue : result.getResult().getVenues()) {
					venues.add(venue);
				}
			} else {
				logError(result);
			}
		} catch (FoursquareApiException e) {
			logger.error("Exception in getVenues", e.getMessage());
		}
		return venues;
	}

	// API to list categories
	@ResponseBody
	@RequestMapping(value = "/categories", method = RequestMethod.GET, headers = "content-type=application/json", produces = "application/json")
	public List<Category> getCategories(Locale locale, @RequestBody Location location) {
		List<Category> categories = null;
		try {
			Result<Category[]> result = foursquareApi.venuesCategories();

			if (result.getMeta().getCode() == 200) {
				logger.info("Categories retreived successfully");
				categories = new ArrayList<Category>();
				for (Category category : result.getResult()) {
					categories.add(category);
				}
			} else {
				logError(result);
			}
		} catch (FoursquareApiException e) {
			logger.error("Exception in getCategories", e.getMessage());
		}
		return categories;
	}

	// API to get venue details by id
	@ResponseBody
	@RequestMapping(value = "/venueDetails/{id}", method = RequestMethod.GET, headers = "content-type=application/json", produces = "application/json")
	public CompleteVenue getVenueDetails(Locale locale, @PathVariable("id") String id) {
		CompleteVenue venueDetails = null;
		try {
			Result<CompleteVenue> result = foursquareApi.venue(id);

			if (result.getMeta().getCode() == 200) {
				logger.info("Retrieved venue details successfully");
				venueDetails = result.getResult();
			} else {
				logError(result);
			}
		} catch (FoursquareApiException e) {
			logger.error("Exception in getVenueDetails", e.getMessage());
		}
		return venueDetails;
	}

	// API to search venues by different set of parameters
	@ResponseBody
	@RequestMapping(value = "/generic/search", method = RequestMethod.GET, headers = "content-type=application/json", produces = "application/json")
	public List<CompactVenue> genericVenueSearch(Locale locale,
			@RequestParam("searchMap") Map<String, String> searchMap) {
		List<CompactVenue> venues = null;
		try {
			Result<VenuesSearchResult> result = foursquareApi.venuesSearch(searchMap);
			if (result.getMeta().getCode() == 200) {
				logger.info("Retrieved venue details successfully");
				venues = new ArrayList<CompactVenue>();
				for (CompactVenue venue : result.getResult().getVenues()) {
					venues.add(venue);
				}
			} else {
				logError(result);
			}

		} catch (FoursquareApiException e) {
			logger.error("Exception in generic venue search", e.getMessage());
		}
		return venues;
	}

	// API to save venue
	@ResponseBody
	@RequestMapping(value = "/add", method = RequestMethod.POST, headers = "content-type=application/json", produces = "application/json")
	public CompleteVenue saveVenue(Locale locale, @RequestBody CompactVenue compactVenue) {
		CompleteVenue venue = null;
		try {
			Location location = compactVenue.getLocation();
			Result<CompleteVenue> result = foursquareApi.venuesAdd(compactVenue.getName(), location.getAddress(),
					location.getCrossStreet(), location.getCity(), location.getState(), location.getPostalCode(),
					compactVenue.getContact().getPhone(), location.getLat() + "," + location.getLng(),
					compactVenue.getCategories().length > 0 ? compactVenue.getCategories()[0].getId() : null);
			if (result.getMeta().getCode() == 200) {
				logger.info("Venue added successfully");
				venue = result.getResult();

			} else {
				logError(result);
			}

		} catch (FoursquareApiException e) {
			logger.error("Exception in add Venue", e.getMessage());
		}
		return venue;
	}

	// API to edit venue
	@ResponseBody
	@RequestMapping(value = "/edit", method = RequestMethod.POST, headers = "content-type=application/json", produces = "application/json")
	public CompleteVenue editVenue(Locale locale, @RequestBody CompactVenue compactVenue) {
		CompleteVenue venue = null;
		try {
			Location location = compactVenue.getLocation();
			Result<Object> result = foursquareApi.venuesEdit(compactVenue.getId(), compactVenue.getName(),
					location.getAddress(), location.getCrossStreet(), location.getCity(), location.getState(),
					location.getPostalCode(), compactVenue.getContact().getPhone(),
					location.getLat() + "," + location.getLng(), null, compactVenue.getContact().getTwitter(),
					compactVenue.getSpecials().length > 0 ? compactVenue.getSpecials()[0].getDescription() : null,
					compactVenue.getUrl());
			if (result.getMeta().getCode() == 200) {
				logger.info("Venue added successfully");
				venue = (CompleteVenue) result.getResult();

			} else {
				logError(result);
			}

		} catch (FoursquareApiException e) {
			logger.error("Exception in editVenue", e.getMessage());
		}
		return venue;
	}

	private <T> void logError(Result<T> result) {
		logger.error("Error occured: ");
		logger.error("  code: " + result.getMeta().getCode());
		logger.error("  type: " + result.getMeta().getErrorType());
		logger.error("  detail: " + result.getMeta().getErrorDetail());
	}

}
