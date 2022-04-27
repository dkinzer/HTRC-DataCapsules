/*******************************************************************************
 * Copyright 2014 The Trustees of Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.indiana.d2i.sloan.bean;

import edu.indiana.d2i.sloan.image.ImageState;

public class ImageInfoBean {
	private String imageId;
	private String imageName;
	private ImageState imageStatus;
	private String imageDescription;
	private String imagePath;
	private String loginUserName;
	private String loginPassWord;
	private String sourceVM;
	private Boolean isPublic;
	private String owner;
	private String createdAt;
	private String updatedAt;

//	public ImageInfoBean(String imageName, ImageState imageStatus, String imageDescription) {
//		this.imageName = imageName;
//		this.imageStatus = imageStatus;
//		this.imageDescription = imageDescription;
//	}

	public ImageInfoBean(String imageId, String imageName, ImageState imageStatus, String imageDescription, String imagePath, String loginUserName,
						 String loginPassWord, String sourceVM, Boolean isPublic, String owner, String createdAt, String updatedAt) {
		this.imageId = imageId;
		this.imageName = imageName;
		this.imageStatus = imageStatus;
		this.imageDescription = imageDescription;
		this.imagePath = imagePath;
		this.loginUserName = loginUserName;
		this.loginPassWord = loginPassWord;
		this.sourceVM = sourceVM;
		this.isPublic = isPublic;
		this.owner = owner;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public String getSourceVM() {
		return sourceVM;
	}

	public void setSourceVM(String sourceVM) {
		this.sourceVM = sourceVM;
	}


	public Boolean getPublic() {
		return isPublic;
	}

	public void setPublic(Boolean aPublic) {
		isPublic = aPublic;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
	public String getImageName() {
		return imageName;
	}
	
	public String getImageDescription() {
		return imageDescription;
	}

	public ImageState getImageStatus() {
		return imageStatus;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public void setImageDescription(String imageDescription) {
		this.imageDescription = imageDescription;
	}

	public void setImageStatus(ImageState imageStatus) {
		this.imageStatus = imageStatus;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	public String getLoginPassWord() {
		return loginPassWord;
	}

	public void setLoginPassWord(String loginPassWord) {
		this.loginPassWord = loginPassWord;
	}
}
