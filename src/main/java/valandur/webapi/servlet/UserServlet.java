package valandur.webapi.servlet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import valandur.webapi.WebAPI;
import valandur.webapi.api.servlet.BaseServlet;
import valandur.webapi.api.servlet.Permission;
import valandur.webapi.security.AuthenticationProvider;
import valandur.webapi.security.PermissionStruct;
import valandur.webapi.security.SecurityContext;
import valandur.webapi.user.UserPermissionStruct;
import valandur.webapi.user.Users;
import valandur.webapi.util.Util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

@Path("user")
@Api(tags = { "User" }, value = "Authenticate and get user information.")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class UserServlet extends BaseServlet {

    @Context
    HttpServletRequest request;

    @GET
    @Permission("user")
    @ApiOperation(
            value = "Check info",
            notes = "Checks to see if the passed api key is still valid and retrieves the user info and " +
                    "permissions associated with this key")
    public PermissionStruct getUserDetails() {
        SecurityContext context = (SecurityContext)request.getAttribute("security");
        return context.getPermissionStruct();
    }

    @POST
    @ApiOperation(
            value = "Login",
            notes = "Tries to acquire an api key with the passed credentials.")
    public PermissionStruct login(AuthRequest req)
            throws ForbiddenException {
        if (req == null) {
            throw new BadRequestException("Request body is required");
        }

        Optional<UserPermissionStruct> optPerm = Users.getUser(req.getUsername(), req.getPassword());
        if (!optPerm.isPresent()) {
            WebAPI.getLogger().warn(req.getUsername() + " tried to login from " +
                    request.getAttribute("ip") + " (invalid username or password)");
            throw new ForbiddenException("Invalid username or password");
        }

        UserPermissionStruct perm = optPerm.get();
        String key = Util.generateUniqueId();

        AuthenticationProvider.addTempPerm(key, perm);

        WebAPI.getLogger().info(req.getUsername() + " logged in from " + request.getAttribute("ip"));

        return perm.withKey(key);
    }

    @POST
    @Path("/logout")
    @ApiOperation(
            value = "Logout",
            notes = "Invalidate the current API key, logging out the active user.")
    public PermissionStruct logout()
            throws ForbiddenException {
        SecurityContext context = (SecurityContext)request.getAttribute("security");
        AuthenticationProvider.removeTempPerm(context.getPermissionStruct().getKey());
        return context.getPermissionStruct();
    }


    @ApiModel("AuthenticationRequest")
        public static class AuthRequest {

        private String username;
        @ApiModelProperty(value = "The username of the user", required = true)
        public String getUsername() {
            return username;
        }

        private String password;
        @ApiModelProperty(value = "The password of the user", required = true)
        public String getPassword() {
            return password;
        }
    }
}
