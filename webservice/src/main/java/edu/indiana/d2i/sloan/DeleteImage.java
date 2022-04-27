package edu.indiana.d2i.sloan;


import edu.indiana.d2i.sloan.bean.ErrorBean;
import edu.indiana.d2i.sloan.bean.ImageInfoBean;
import edu.indiana.d2i.sloan.bean.VmInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.hyper.DeleteImageCommand;
import edu.indiana.d2i.sloan.hyper.HypervisorProxy;
import edu.indiana.d2i.sloan.image.ImageState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/deleteimage")
public class DeleteImage {
    private static final Logger logger = LoggerFactory.getLogger(DeleteImage.class);

    @DELETE
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateImage(@FormParam("imageId") String imageId,
                                @Context HttpHeaders httpHeaders,
                                @Context HttpServletRequest httpServletRequest) {
        String userName = httpServletRequest.getHeader(Constants.USER_NAME);

        // check if username exists
        if (userName == null) {
            logger.error("Username is not present in http header.");
            return Response
                    .status(500)
                    .entity(new ErrorBean(500,
                            "Username is not present in http header.")).build();
        }

        if (imageId == null) {
            logger.error("Image ID is null!");
            return Response.status(400).entity(
                    new ErrorBean(400, "Image ID cannot be empty!")).build();
        }

        try {
            ImageInfoBean imageInfo = DBOperations.getInstance().getImageInfo(imageId);
            VmInfoBean vmInfoBean = DBOperations.getInstance().getAllVmInfoByID(imageInfo.getSourceVM());
            if(imageInfo.getImageStatus() != ImageState.DELETE_PENDING){
                logger.error("Cannot DELETE the image with the image ID " + imageId+ " when it's not in the " + ImageState.DELETE_PENDING.toString() + " state.");
                return Response
                        .status(400)
                        .entity(new ErrorBean(400, "Cannot DELETE the image with the image ID " + imageId+ " when it's not in " + ImageState.DELETE_PENDING.toString() + " state."))
                        .build();
            }
            if(!imageInfo.getOwner().equals(userName)){
                logger.error("User " + userName + " is not allowed to delete the image with image ID " + imageId);
                return Response.status(400).entity( "User " + userName + " is not allowed to delete the image with image ID " + imageId).build();
            }
            HypervisorProxy.getInstance().addCommand(new DeleteImageCommand(vmInfoBean,imageInfo,userName));
            logger.info("Deleted the image with the ID " + imageId );
            return Response.status(200).build();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Response.status(500)
                    .entity(new ErrorBean(500, e.getMessage())).build();
        }
    }
}
