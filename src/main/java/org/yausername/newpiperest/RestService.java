package org.yausername.newpiperest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.schabi.newpipe.extractor.InfoItem;
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage;
import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.Page;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelInfo;
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfo;
import org.schabi.newpipe.extractor.comments.CommentsInfoItem;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.kiosk.KioskInfo;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistInfo;
import org.schabi.newpipe.extractor.search.SearchInfo;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

import com.google.gson.Gson;

public class RestService {
    
    private Gson gson = new Gson();
    
    public RestService() {
        DownloaderImpl d = DownloaderImpl.init(null);
        NewPipe.init(d, new Localization("en", "GB"));
    }
    
    public String getServices() {
        return gson.toJson(NewPipe.getServices());
    }
    
    public String getSearchInfo(int serviceId, String searchString, List<String> contentFilters, String sortFilter)
            throws ParsingException, ExtractionException, IOException {
        StreamingService service = NewPipe.getService(serviceId);
        SearchInfo info = SearchInfo.getInfo(service,
                service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter));
        return gson.toJson(info);
    }
    
    public String getSearchPage(int serviceId, String searchString, List<String> contentFilters, String sortFilter,
            String pageUrl) throws ParsingException, ExtractionException, IOException {
        StreamingService service = NewPipe.getService(serviceId);
        // FIXED: pageUrl is now a Page object
        Page page = new Page(pageUrl);
        InfoItemsPage<InfoItem> pageResult = SearchInfo.getMoreItems(service,
                service.getSearchQHFactory().fromQuery(searchString, contentFilters, sortFilter), page);
        return gson.toJson(pageResult);
    }
    
    public String getKioskIdsList(int serviceId) throws ExtractionException {
        StreamingService service = NewPipe.getService(serviceId);
        List<String> res = new ArrayList<>();
        service.getKioskList().getAvailableKiosks().forEach(k -> res.add(k));
        return gson.toJson(res);
    }
    
    public String getKioskInfo(int serviceId, String kioskId) throws ExtractionException, IOException {
        StreamingService service = NewPipe.getService(serviceId);
        String url = service.getKioskList().getListLinkHandlerFactoryByType(kioskId).getUrl(kioskId);
        KioskInfo info = KioskInfo.getInfo(service, url);
        return gson.toJson(info);
    }
    
    public String getKioskPage(int serviceId, String kioskId, String pageUrl) throws ExtractionException, IOException {
        StreamingService service = NewPipe.getService(serviceId);
        String url = service.getKioskList().getListLinkHandlerFactoryByType(kioskId).getUrl(kioskId);
        // FIXED: pageUrl is now a Page object
        Page page = new Page(pageUrl);
        InfoItemsPage<StreamInfoItem> pageResult = KioskInfo.getMoreItems(service, url, page);
        return gson.toJson(pageResult);
    }
    
    public String getStreamInfo(@Nonnull String url) throws IOException, ExtractionException {
        StreamInfo info = StreamInfo.getInfo(url);
        return gson.toJson(info);
    }
    
    public String getPlaylistInfo(@Nonnull String url) throws IOException, ExtractionException {
        PlaylistInfo info = PlaylistInfo.getInfo(url);
        return gson.toJson(info);
    }
    
    public String getPlaylistPage(@Nonnull String url, @Nonnull String pageUrl) throws IOException, ExtractionException {
        StreamingService service = NewPipe.getServiceByUrl(url);
        // FIXED: pageUrl is now a Page object
        Page page = new Page(pageUrl);
        InfoItemsPage<StreamInfoItem> pageResult = PlaylistInfo.getMoreItems(service, url, page);
        return gson.toJson(pageResult);
    }
    
    public String getChannelInfo(@Nonnull String url) throws IOException, ExtractionException {
        ChannelInfo info = ChannelInfo.getInfo(url);
        return gson.toJson(info);
    }
    
    public String getChannelPage(@Nonnull String url, @Nonnull String pageUrl) throws IOException, ExtractionException {
        // FIXED: ChannelInfo.getMoreItems no longer exists in v0.24.8
        // Need to use ChannelTabInfo instead
        ChannelInfo channelInfo = ChannelInfo.getInfo(url);
        
        // Get the first tab (usually videos)
        if (channelInfo.getTabs() != null && !channelInfo.getTabs().isEmpty()) {
            ChannelTabInfo tabInfo = ChannelTabInfo.getInfo(
                NewPipe.getServiceByUrl(url), 
                channelInfo.getTabs().get(0)
            );
            
            Page page = new Page(pageUrl);
            InfoItemsPage<StreamInfoItem> pageResult = ChannelTabInfo.getMoreItems(
                NewPipe.getServiceByUrl(url), 
                channelInfo.getTabs().get(0), 
                page
            );
            return gson.toJson(pageResult);
        }
        
        return gson.toJson(new InfoItemsPage<>(null, new ArrayList<>(), null));
    }
    
    public String getCommentsInfo(@Nonnull String url) throws IOException, ExtractionException {
        CommentsInfo info = CommentsInfo.getInfo(url);
        return gson.toJson(info);
    }
    
    public String getCommentsPage(@Nonnull String url, @Nonnull String pageUrl) throws IOException, ExtractionException {
        CommentsInfo info = CommentsInfo.getInfo(url);
        // FIXED: pageUrl is now a Page object
        Page page = new Page(pageUrl);
        InfoItemsPage<CommentsInfoItem> pageResult = CommentsInfo.getMoreItems(info, page);
        return gson.toJson(pageResult);
    }
    
    public String getError(Exception e) {
        return gson.toJson(new Error(e.getMessage()));
    }
}
