package com.pubnub.api.endpoints.objects_api.spaces;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.models.consumer.objects_api.util.InclusionParamsProvider;
import com.pubnub.api.enums.PNSpaceFields;
import com.pubnub.api.models.consumer.objects_api.space.PNSpace;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.models.consumer.objects_api.space.PNUpdateSpaceResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class UpdateSpace extends Endpoint<EntityEnvelope<PNSpace>, PNUpdateSpaceResult>
        implements InclusionParamsProvider<UpdateSpace, PNSpaceFields> {

    private Map<String, String> extraParamsMap;

    @Setter
    private PNSpace space;

    public UpdateSpace(PubNub pubnubInstance, TelemetryManager telemetry, RetrofitManager retrofitInstance) {
        super(pubnubInstance, telemetry, retrofitInstance);
        extraParamsMap = new HashMap<>();
    }

    @Override
    protected List<String> getAffectedChannels() {
        return null;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null
                || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }

        if (this.space == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SPACE_MISSING).build();
        }

        if (this.space.getId() == null || this.space.getId().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SPACE_ID_MISSING).build();
        }

        // todo validate custom json
    }

    @Override
    protected Call<EntityEnvelope<PNSpace>> doWork(Map<String, String> params) {

        params.putAll(extraParamsMap);

        params.putAll(encodeParams(params));

        return this.getRetrofit()
                .getSpaceService()
                .updateSpace(this.getPubnub().getConfiguration().getSubscribeKey(), space.getId(), space, params);
    }

    @Override
    protected PNUpdateSpaceResult createResponse(Response<EntityEnvelope<PNSpace>> input) throws PubNubException {
        PNUpdateSpaceResult.PNUpdateSpaceResultBuilder resultBuilder = PNUpdateSpaceResult.builder();

        if (input.body() != null) {
            resultBuilder.space(input.body().getData());
        }
        return resultBuilder.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNUpdateSpaceOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    @Override
    public UpdateSpace includeFields(PNSpaceFields... params) {
        return appendInclusionParams(extraParamsMap, params);
    }
}
