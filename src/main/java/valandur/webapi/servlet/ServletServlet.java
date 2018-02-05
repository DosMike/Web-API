/*package valandur.webapi.servlet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import valandur.webapi.api.servlet.BaseServlet;
import valandur.webapi.api.servlet.IServletService;
import valandur.webapi.api.servlet.Permission;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;

@Path("servlet")
@Api(tags = { "Servlet" }, value = "Get information about the runnings servlets on the server.")
public class ServletServlet extends BaseServlet {

    @GET
    @Permission("list")
    @ApiOperation("List servlets")
    public Map<String, IServletService.ServletInfo> getServlets() {
        return servletService.getLoadedServlets();
    }
}
*/