package edu.indiana.d2i.sloan;


import edu.indiana.d2i.sloan.bean.ErrorBean;
import edu.indiana.d2i.sloan.bean.ImageInfoBean;
import edu.indiana.d2i.sloan.db.DBOperations;
import edu.indiana.d2i.sloan.image.ImageState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/activateimage")
public class ActivateImage {
    private static final Logger logger = LoggerFactory.getLogger(ActivateImage.class);

    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateImage(@FormParam("imageId") String imageId,
                                @Context HttpHeaders httpHeaders,
                                @Context HttpServletRequest httpServletRequest) {

        if (imageId == null) {
            logger.error("Image ID is null!");
            return Response.status(400).entity(
                    new ErrorBean(400, "Image ID cannot be empty!")).build();
        }

        try {
            ImageInfoBean imageInfo = DBOperations.getInstance().getImageInfo(imageId);
            if(imageInfo.getImageStatus() != ImageState.SHARE_PENDING){
                logger.error("Cannot update the image with the image ID " + imageId+ " when it's not in the " + ImageState.SHARE_PENDING.toString() + " state.");
                return Response
                        .status(400)
                        .entity(new ErrorBean(400, "Cannot update the image with the image ID " + imageId + " when it's not in " + ImageState.SHARE_PENDING.toString() + " state."))
                        .build();
            }
            DBOperations.getInstance().updateImageState(imageId, ImageState.ACTIVE);
            imageInfo.setImageStatus(ImageState.ACTIVE);
            logger.info("Activated the image with the ID " + imageId );
            return Response.status(200).entity(imageInfo).build();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return Response.status(500)
                    .entity(new ErrorBean(500, e.getMessage())).build();
        }
    }
}
