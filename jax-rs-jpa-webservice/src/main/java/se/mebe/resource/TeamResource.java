package se.mebe.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.springframework.stereotype.Component;
import se.mebe.converter.EntityConverter;
import se.springdata.exception.ServiceException;
import se.springdata.repository.TeamRepository;
import se.springdata.repository.UserRepository;
import se.springdata.service.TeamService;

@Component
@Path("/teams")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON })

public final class TeamResource {

	private final TeamService teamService;
	private final EntityConverter entityConverter;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;
	private Collection<se.springdata.model.Team> teams;
	private List<se.mebe.model.Team> teamsJson;
	private se.springdata.model.Team newTeam;
	private se.mebe.model.Team allTeams;
	private URI location;

	@Context
	private UriInfo uriInfo;
	@Context
	private HttpHeaders headers;

	public TeamResource(TeamService teamService, EntityConverter entityConverter, TeamRepository teamRepository,
			UserRepository userRepository) {
		this.teamService = teamService;
		this.entityConverter = entityConverter;
		this.teamRepository = teamRepository;
		this.userRepository = userRepository;
		this.teamsJson = new ArrayList<>();
	}

	@POST
	public Response addTeam(se.mebe.model.Team team) throws ServiceException {

		newTeam = teamService.addTeam(entityConverter.convertToJpaTeam(team));
		location = uriInfo.getAbsolutePathBuilder().path(newTeam.getId().toString()).build();

		return Response.created(location).build();
	}

	@PUT
	@Path("update/{teamName}/{teamId}")
	public Response updateTeam(@PathParam("teamName") String teamName, @PathParam("teamId") long teamId)
			throws ServiceException {

		if (!teamRepository.exists(teamId)) {
			return Response.status(Status.BAD_REQUEST).entity("This team dosnt exist for update")
					.type(MediaType.TEXT_PLAIN).build();
		} else {
			teamService.updateTeam(teamName, teamId);
			return Response.noContent().build();
		}
	}

	@PUT
	@Path("state/{id}/{status}")
	public Response inactiveTeam(@PathParam("id") long teamId, @PathParam("status") String status)
			throws ServiceException {
		if (!teamRepository.exists(teamId)) {
			return Response.status(Status.BAD_REQUEST).entity("This team dosnt exist for update")
					.type(MediaType.TEXT_PLAIN).build();

		} else if (teamRepository.findTeamStatusById(teamId).equals(status)) {
			return Response.status(Status.BAD_REQUEST).entity("This team is already have this status")
					.type(MediaType.TEXT_PLAIN).build();
		} else {

			teamService.setTeamStatus(teamId, status);
			return Response.noContent().build();
		}
	}

	@GET
	@Path("/all")
	public Response findAllTeams() throws ServiceException {
		if (teamRepository.findAllTeams().isEmpty()) {
			return Response.status(Status.NOT_FOUND).entity("There is not any team in your table")
					.type(MediaType.TEXT_PLAIN).build();
		} else {

			teams = teamService.getAllTeams();
			teams.forEach(team -> {

				allTeams = entityConverter.convertToRestTeam(team);
				teamsJson.add(allTeams);
			});

			return Response.ok(teamsJson, headers.getMediaType()).build();
		}
	}

	@PUT
	@Path("/asign/{teamId}/{userId}")
	public Response asignUserToTeam(@PathParam("teamId") long teamId, @PathParam("userId") long userId)
			throws ServiceException {
		if (!userRepository.exists(userId)) {
			return Response.status(Status.NOT_FOUND).entity("This user dosnt exist to assign it to team")
					.type(MediaType.TEXT_PLAIN).build();

		} else if (!teamRepository.exists(teamId)) {
			return Response.status(Status.NOT_FOUND).entity("This team do not exist to assign user to !!")
					.type(MediaType.TEXT_PLAIN).build();
		} else {

			teamService.addUserToTeam(teamId, userId);
			return Response.noContent().build();
		}
	}
}
