/**
 * Copyright 2011 Henric Persson (henric.persson@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package burrito.server.blobstore;

import burrito.client.dto.BlobInfoDTO;
import burrito.client.widgets.services.BlobService;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class BlobServiceImpl extends RemoteServiceServlet implements BlobService {
	private static final long serialVersionUID = 1L;

	private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

	@Override
	public String getBlobStoreUploadURL() {
		String uploadUrl = blobstoreService.createUploadUrl("/admin/blobstore/upload");

		if (uploadUrl.startsWith("http://0.0.0.0")) {
			uploadUrl = "http://localhost" + uploadUrl.substring(14);
		}
		
		return uploadUrl;
	}
	
	public BlobInfoDTO getFileInfo(String blobKeyString) {
		BlobKey blobKey = new BlobKey(blobKeyString);
		
		
		BlobInfoFactory blobInfoFactory = new BlobInfoFactory();
		BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
		if (blobInfo != null) {
			return createDTO(blobInfo);
		}
		return null;
	}
	
	private BlobInfoDTO createDTO(BlobInfo blobInfo) {
		BlobInfoDTO dto = new BlobInfoDTO();
		dto.setFilename(blobInfo.getFilename());
		dto.setContentType(blobInfo.getContentType());
		dto.setSize(blobInfo.getSize());
		dto.setCreation(blobInfo.getCreation());
		dto.setBlobKey(blobInfo.getBlobKey().getKeyString());
		return dto;
	}
}
