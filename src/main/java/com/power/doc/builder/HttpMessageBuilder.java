package com.power.doc.builder;

import com.google.gson.Gson;
import com.power.common.util.OkHttp3Util;
import com.power.doc.constants.TornaConstants;
import com.power.doc.factory.BuildTemplateFactory;
import com.power.doc.helper.JavaProjectBuilderHelper;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import com.power.doc.model.torna.Apis;
import com.power.doc.template.IDocBuildTemplate;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.power.doc.utils.TornaUtil.*;

/**
 * @author biding
 */
public class HttpMessageBuilder {


    /**
     * build controller api
     *
     * @param config config
     */
    public static void buildApiDoc(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = JavaProjectBuilderHelper.create();
        buildApiDoc(config, javaProjectBuilder);
    }


    /**
     * Only for smart-doc maven plugin and gradle plugin.
     *
     * @param config             ApiConfig
     * @param javaProjectBuilder ProjectDocConfigBuilder
     */
    public static void buildApiDoc(ApiConfig config, JavaProjectBuilder javaProjectBuilder) {
        config.setParamsDataToTree(true);
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config, true);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<ApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        apiDocList = docBuildTemplate.handleApiGroup(apiDocList, config);
        buildTorna(apiDocList, config, javaProjectBuilder);
    }

    /**
     * build torna Data
     *
     * @param apiDocs   apiData
     * @param apiConfig ApiConfig
     * @param builder   JavaProjectBuilder
     */
    public static void buildTorna(List<ApiDoc> apiDocs, ApiConfig apiConfig, JavaProjectBuilder builder) {
        if (apiDocs.size() == 0) {
            return;
        }

        ApiDoc doc = apiDocs.get(0);
        List<Apis> items = new ArrayList<>();
        for (ApiDoc a : doc.getChildrenApiDocs()) {
            final Apis api = new Apis();
            api.setName(StringUtils.isBlank(a.getDesc()) ? a.getName() : a.getDesc());
            api.setItems(buildApis(a.getList(), false));
            api.setIsFolder(TornaConstants.YES);
            api.setAuthor(a.getAuthor());
            api.setOrderIndex(a.getOrder());
            api.setClassName(a.getName());
            api.setPackageName(a.getPackageName());
            items.add(api);
        }
        final Apis api = new Apis();
        api.setName(StringUtils.defaultIfBlank(apiConfig.getServiceName(), "UNKNOWN"));
        api.setOrderIndex(doc.getOrder());
        api.setIsFolder(TornaConstants.YES);
        api.setItems(items);

        //Get the response result
        String responseMsg = OkHttp3Util.syncPostJson(apiConfig.getOpenUrl(), new Gson().toJson(api));
        // print response message
        System.out.println(responseMsg);
    }
}
