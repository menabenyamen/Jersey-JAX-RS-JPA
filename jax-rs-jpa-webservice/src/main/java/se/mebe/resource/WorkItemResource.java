package se.mebe.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import org.springframework.stereotype.Component;
import se.mebe.converter.EntityConverter;
import se.springdata.exception.ServiceException;
//import se.springdata.model.Status;
import se.springdata.repository.IssueRepository;
import se.springdata.repository.UserRepository;
import se.springdata.repository.WorkitemRepository;
import se.springdata.service.WorkitemService;

@Component
@Path("/items")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })

public final class WorkItemResource {

	private final WorkitemService workItemService;
	private final EntityConverter entityConverter;
	private final WorkitemRepository workitemRepository;
	private final IssueRepository issueRepository;
	private final UserRepository userRepository;
	private se.springdata.model.Workitem newWorkItem;
	private Collection<se.springdata.model.Workitem> workItems;
	private se.mebe.model.WorkItem jsonWorkItem;
	private List<se.mebe.model.WorkItem> putWorkitems;
	private URI location;

	@Context
	private UriInfo uriInfo;
	@Context
	private HttpHeaders headers;

	public WorkItemResource(WorkitemService workItemService, EntityConverter entityConverter,
			WorkitemRepository workitemRepository, IssueRepository issueRepository, UserRepository userRepository) {

		this.workitemRepository = workitemRepository;
		this.issueRepository = issueRepository;
		this.workItemService = workItemService;
		this.entityConverter = entityConverter;
		this.userRepository = userRepository;
		this.putWorkitems = new ArrayList<>();
	}

	@POST
	public Response addWorkItem(se.mebe.model.WorkItem workItem) throws ServiceException {
		newWorkItem = workItemService.addWorkItem(entityConverter.convertToWorkItemEntity(workItem));
		location = uriInfo.getAbsolutePathBuilder().path(newWorkItem.getId().toString()).build();
		return Response.created(location).build();
	}

	@PUT
	@Path("/state/{id}/{status}")
	public Response updateWorkItem(@PathParam("id") Long workItemId,
			@PathParam("status") se.springdata.model.Status status) throws ServiceException {

		if (!workitemRepository.exists(workItemId)) {
			return Response.status(Status.BAD_REQUEST).entity("You are trying with not existing workitem")
					.type(MediaType.TEXT_PLAIN).build();

		} else if (workitemRepository.findWorkitemStatusById(workItemId).iterator().next().equals(status)) {
			return Response.status(Status.BAD_REQUEST).entity("You are trying to update to same information")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItemService.setStatus(workItemId, status);
			return Response.noContent().build();
		}
	}

	@DELETE
	@Path("/remove/{issueId}/{id}")
	public Response removeWorkItem(@PathParam("issueId") long issueId, @PathParam("id") long workItemId)
			throws ServiceException {
		if (!workitemRepository.exists(workItemId)) {
			return Response.status(Status.BAD_REQUEST).entity("This work item dosnt exist to delet")
					.type(MediaType.TEXT_PLAIN).build();

		} else if (!issueRepository.exists(issueId)) {
			return Response.status(Status.BAD_REQUEST).entity("This work item dosnt have issue to delet")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItemService.deleteWorkitem(issueId, workItemId);
			return Response.noContent().build();
		}
	}

	@PUT 
	@Path("/assign/{userId}/{workItemId}")
	public Response assignWorkItemToUser(@PathParam("userId") long userId, @PathParam("workItemId") long workItemId)
			throws ServiceException {

		if (!workitemRepository.exists(workItemId)) {
			return Response.status(Status.BAD_REQUEST).entity("This work item dosnt exist to assign to user")
					.type(MediaType.TEXT_PLAIN).build();

		} else if (!userRepository.exists(userId)) {
			return Response.status(Status.BAD_REQUEST).entity("This user dosnt exist to assign work item to it")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItemService.assignsWorkitemToUser(userId, workItemId);
			return Response.noContent().build();
		}
	}

	@GET 
	@Path("/state/{status}")
	public Response findAllWorkItemsByState(@PathParam("status") se.springdata.model.Status status)
			throws ServiceException {

		if (workitemRepository.findWorkitemStatusByStatus(status).isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("There is no such status in work item")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItems = workItemService.getAllWorkitemsByStatus(status);
			workItems.forEach(workItem -> {
				jsonWorkItem = entityConverter.convertToRestWorkItem(workItem);
				putWorkitems.add(jsonWorkItem);

			});

			return Response.ok(putWorkitems, headers.getMediaType()).build();
		}
	}

	@GET 
	@Path("/team/{teamId}")
	public Response findAllWorkItemsByTeam(@PathParam("teamId") long teamId) throws ServiceException {

		if (workitemRepository.findTeamIdFromWorkitem().isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("There is no such team in work item")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItems = workItemService.getAllWorkitemsByTeam(teamId);
			workItems.forEach(workItem -> {
				jsonWorkItem = entityConverter.convertToRestWorkItem(workItem);
				putWorkitems.add(jsonWorkItem);
			});

			return Response.ok(putWorkitems, headers.getMediaType()).build();
		}
	}

	@GET 
	@Path("/user/{userId}")
	public Response findAllWorkItemsByUser(@PathParam("userId") long userId) throws ServiceException {

		if (workitemRepository.findUserIdFromWorkitem().isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("There is no such user in work item")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItems = workItemService.getAllWorkitemsByUser(userId);
			workItems.forEach(workItem -> {
				jsonWorkItem = entityConverter.convertToRestWorkItem(workItem);
				putWorkitems.add(jsonWorkItem);
			});
			return Response.ok(putWorkitems, headers.getMediaType()).build();
		}
	}

	@GET
	@Path("/search/{searchValue}")
	public Response findWorkItemByDescription(@PathParam("searchValue") String searchValue) throws ServiceException {

		if (workitemRepository.findAllByCertainDescription(searchValue).isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("There is no such description in work item")
					.type(MediaType.TEXT_PLAIN).build();

		} else {
			workItems = workItemService.searchByDescription(searchValue);
			workItems.forEach(workItem -> {
				jsonWorkItem = entityConverter.convertToRestWorkItem(workItem);
				putWorkitems.add(jsonWorkItem);
			});
			return Response.ok(putWorkitems, headers.getMediaType()).build();
		}

	}
}
