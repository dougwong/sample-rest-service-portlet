/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.samplerestservice.rest;

import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.model.Group;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.blogs.model.BlogsEntry;
import com.liferay.portlet.blogs.service.BlogsEntryLocalServiceUtil;

import java.io.InputStream;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * @author Brian Wing Shun Chan
 * @author Peter Shin
 * @author Douglas Wong
 */
public class SampleRestService {

	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@POST
	@Path("blogs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addBlogsEntry(
		@Context HttpServletRequest request, @Context UriInfo uriInfo,
		@FormParam("title") String title,
		@FormParam("content") String content) {

		ResponseBuilder responseBuilder = null;

		try {
			long companyId = PortalUtil.getCompanyId(request);

			Calendar calendar = CalendarFactoryUtil.getCalendar();

			calendar.setTime(new Date());

			long userId = UserLocalServiceUtil.getDefaultUserId(companyId);
			String description = null;
			int displayDateMonth = calendar.get(Calendar.MONTH);
			int displayDateDay = calendar.get(Calendar.DATE);
			int displayDateYear = calendar.get(Calendar.YEAR);
			int displayDateHour = calendar.get(Calendar.HOUR_OF_DAY);
			int displayDateMinute = calendar.get(Calendar.MINUTE);
			boolean allowPingbacks = false;
			boolean allowTrackbacks = false;
			String[] trackbacks = null;
			boolean smallImage = false;
			String smallImageURL = null;
			String smallImageFileName = null;
			InputStream smallImageInputStream = null;

			ServiceContext serviceContext = new ServiceContext();

			Group group = GroupLocalServiceUtil.getCompanyGroup(companyId);

			serviceContext.setScopeGroupId(group.getGroupId());

			BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.addEntry(
				userId, title, description, content, displayDateMonth,
				displayDateDay, displayDateYear, displayDateHour,
				displayDateMinute, allowPingbacks, allowTrackbacks, trackbacks,
				smallImage, smallImageURL, smallImageFileName,
				smallImageInputStream, serviceContext);

			UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

			uriBuilder = uriBuilder.path("blogs");

			uriBuilder = uriBuilder.path(
				String.valueOf(blogsEntry.getEntryId()));

			responseBuilder = Response.created(uriBuilder.build());

			JSONObject jsonObject = getBlogsEntryJSONObject(blogsEntry);

			responseBuilder = responseBuilder.entity(jsonObject.toString(4));

			return responseBuilder.build();
		}
		catch (Exception e) {
			responseBuilder = Response.status(
				Response.Status.INTERNAL_SERVER_ERROR);

			responseBuilder.entity(JSONFactoryUtil.serializeThrowable(e));

			return responseBuilder.build();
		}
	}

	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@DELETE
	@Path("blogs/{entryId}")
	public Response deleteBlogsEntry(
		@Context HttpServletRequest request,
		@PathParam("entryId") Long entryId) {

		ResponseBuilder responseBuilder = null;

		try {
			BlogsEntryLocalServiceUtil.deleteEntry(entryId);

			responseBuilder = Response.noContent();

			return responseBuilder.build();
		}
		catch (Exception e) {
			responseBuilder = Response.status(
				Response.Status.INTERNAL_SERVER_ERROR);

			responseBuilder.entity(JSONFactoryUtil.serializeThrowable(e));

			return responseBuilder.build();
		}
	}

	@Consumes(MediaType.TEXT_PLAIN)
	@GET
	@Path("blogs")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBlogsEntries(
		@QueryParam("start") Integer start, @QueryParam("end") Integer end) {

		ResponseBuilder responseBuilder = null;

		try {
			JSONArray jsonArray = JSONFactoryUtil.createJSONArray();

			List<BlogsEntry> blogsEntries =
				BlogsEntryLocalServiceUtil.getBlogsEntries(start, end);

			for (BlogsEntry blogsEntry : blogsEntries) {
				jsonArray.put(getBlogsEntryJSONObject(blogsEntry));
			}

			responseBuilder = Response.ok(
				jsonArray.toString(4), MediaType.APPLICATION_JSON);

			return responseBuilder.build();
		}
		catch (Exception e) {
			responseBuilder = Response.status(
				Response.Status.INTERNAL_SERVER_ERROR);

			responseBuilder.entity(JSONFactoryUtil.serializeThrowable(e));

			return responseBuilder.build();
		}
	}

	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@PUT
	@Path("blogs/{entryId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateBlogsEntry(
		@Context HttpServletRequest request, @PathParam("entryId") Long entryId,
		@FormParam("title") String title,
		@FormParam("content") String content) {

		ResponseBuilder responseBuilder = null;

		try {
			long companyId = PortalUtil.getCompanyId(request);
			long userId = UserLocalServiceUtil.getDefaultUserId(companyId);
			Group group = GroupLocalServiceUtil.getCompanyGroup(companyId);

			Calendar calendar = CalendarFactoryUtil.getCalendar();

			calendar.setTime(new Date());

			ServiceContext serviceContext = new ServiceContext();

			serviceContext.setScopeGroupId(group.getGroupId());

			BlogsEntry blogsEntry = BlogsEntryLocalServiceUtil.updateEntry(
				userId, entryId, title, null, content,
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),
				calendar.get(Calendar.YEAR), calendar.get(Calendar.HOUR_OF_DAY),
				calendar.get(Calendar.MINUTE), false, false, null, false, null,
				null, null, serviceContext);

			JSONObject jsonObject = getBlogsEntryJSONObject(blogsEntry);

			responseBuilder = Response.ok(
				jsonObject.toString(4), MediaType.APPLICATION_JSON);

			return responseBuilder.build();
		}
		catch (Exception e) {
			responseBuilder = Response.status(
				Response.Status.INTERNAL_SERVER_ERROR);

			responseBuilder.entity(JSONFactoryUtil.serializeThrowable(e));

			return responseBuilder.build();
		}
	}

	protected JSONObject getBlogsEntryJSONObject(BlogsEntry blogsEntry) {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();

		jsonObject.put("entryId", blogsEntry.getEntryId());
		jsonObject.put("groupId", blogsEntry.getGroupId());
		jsonObject.put("title", blogsEntry.getTitle());
		jsonObject.put("content", blogsEntry.getContent());

		return jsonObject;
	}

}