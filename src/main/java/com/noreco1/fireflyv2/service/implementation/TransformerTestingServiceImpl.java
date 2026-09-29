package com.noreco1.fireflyv2.service.implementation;

import com.noreco1.fireflyv2.common.facade.AuthenticationFacade;
import com.noreco1.fireflyv2.common.facade.DocumentLoggerFacade;
import com.noreco1.fireflyv2.common.facade.DocumentProcessingFacade;
import com.noreco1.fireflyv2.common.facade.GeneratorFacade;
import com.noreco1.fireflyv2.common.helpers.Checker;
import com.noreco1.fireflyv2.common.helpers.ClassHelper;
import com.noreco1.fireflyv2.common.helpers.MessageFormatter;
import com.noreco1.fireflyv2.common.helpers.ReportUtil;
import com.noreco1.fireflyv2.controller.response.PostResponse;
import com.noreco1.fireflyv2.controller.response.ProcessDocumentDto;
import com.noreco1.fireflyv2.exception.BusinessException;
import com.noreco1.fireflyv2.model.*;
import com.noreco1.fireflyv2.model.enums.DocumentToTableMap;
import com.noreco1.fireflyv2.mssql_repo.MssqlTransformerRepo;
import com.noreco1.fireflyv2.mssql_repo.MssqlUserRepo;
import com.noreco1.fireflyv2.repo.*;
import com.noreco1.fireflyv2.service.TransformerTestingService;
import com.noreco1.fireflyv2.validator.TransformerTestingValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransformerTestingServiceImpl implements TransformerTestingService {

    private final TransformerTestingRepo transformerTestingRepo;
    private final AuthenticationFacade authenticationFacade;
    private final TransformerRepo transformerRepo;
    private final UserRepo userRepo;
    private final TransformerVoltageRatioTestRepo transformerVoltageRatioTestRepo;
    private final TransformerLossTestRepo transformerLossTestRepo;
    private final GeneratorFacade generatorFacade;
    private final DocumentLoggerFacade documentLoggerFacade;
    private final DocumentWorkflowActionMapRepo documentWorkflowActionMapRepo;
    private final DocumentProcessingFacade documentProcessingFacade;
    private final MssqlTransformerRepo mssqlTransformerRepo;
    private final MssqlUserRepo mssqlUserRepo;
    private final EmployeeRepo employeeRepo;

    @Override
    @Transactional(readOnly = true)
    public TransformerTesting getById(Integer id) {
        TransformerTesting data = transformerTestingRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Transformer testing with id:"+id+" not found!"));

        data.setVoltageRatioTests(transformerVoltageRatioTestRepo.findAllByTransformerTestingId(data.getId()));
        data.setLossTests(transformerLossTestRepo.findAllByTransformerTestingId(data.getId()));

        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransformerTesting> findAll(Pageable pageable) {
        return transformerTestingRepo.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransformerTesting> findAllByQuery(String query, Pageable pageable) {
        return transformerTestingRepo.findAllByTransformerSerialNoContainingIgnoreCase(query, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public HashMap<String, Object> transformerTestingParameters(HttpServletRequest request, Integer id) {
        HashMap<String, Object> params = ReportUtil.setupSharedReportHeaders(request);

        TransformerTesting data = transformerTestingRepo.findById(id)
                .orElseThrow(() -> new BusinessException("Transformer testing with id:"+id+" not found!", HttpStatus.NOT_FOUND));

        TransformerVoltageRatioTest voltageRatioTest = transformerVoltageRatioTestRepo.findByTransformerTestingId(id);
        TransformerLossTest lossTest = transformerLossTestRepo.findByTransformerTestingId(id);

        Transformer transformer = data.getTransformer();

        Employee testedByPosition = employeeRepo.findOneByAccountNumber(data.getTestedBy().getAccountNo());
        Employee recommendingApprovalPosition = employeeRepo.findOneByAccountNumber(data.getRecommendingApprovalUser().getAccountNo());
        Employee approvedByPosition = employeeRepo.findOneByAccountNumber(data.getApprovedBy().getAccountNo());

        params.put("TITLE", "TRANSFORMER TEST RESULT");
        params.put("OWNER", data.getOwner());
        params.put("OWNER_ADDRESS", data.getOwnerAddress());
        params.put("DATE_TESTED", data.getDateTested());
        params.put("TIME_TESTED", data.getTimeTested());
        params.put("WEATHER", data.getWeather());

        params.put("MAKE", transformer.getBrand() != null ? transformer.getBrand().getName() : null);
        params.put("SERIAL_NO", transformer.getSerialNo());
        params.put("KVA", transformer.getKva());
        params.put("IMPEDANCE", transformer.getImpedance());
        params.put("PRI_VOLTAGE", transformer.getPrimaryVoltage() !=  null ? transformer.getPrimaryVoltage().getDescription() : null);
        params.put("SEC_VOLTAGE", transformer.getSecondaryVoltage() !=  null ? transformer.getSecondaryVoltage().getDescription() : null);
        params.put("CORE_TYPE", transformer.getCoreType());
        params.put("POLARITY", transformer.getPolarity());
        params.put("BUSHING", transformer.getBushing());
        params.put("TYPE", transformer.getType());
        params.put("STATUS", data.getTransformerCondition() != null ? data.getTransformerCondition().getDescription() : null);

        params.put("PRI_VOLTAGE_INDUCE", voltageRatioTest.getPrimaryVoltageInduce());
        params.put("TAP1", voltageRatioTest.getTap1());
        params.put("TAP2", voltageRatioTest.getTap2());
        params.put("TAP3", voltageRatioTest.getTap3());
        params.put("TAP4", voltageRatioTest.getTap4());
        params.put("TAP5", voltageRatioTest.getTap5());

        params.put("SC_PRI_CURRENT", lossTest.getShortCircuitPrimaryCurrent());
        params.put("SC_RESULT", lossTest.getShortCircuitResult());
        params.put("OC_SEC_VOLTAGE", lossTest.getOpenCircuitSecondaryVoltage());
        params.put("OC_RESULT", lossTest.getOpenCircuitResult());
        params.put("TOTAL_LOSSES", lossTest.getTotalLoss());
        params.put("IEX", lossTest.getIex());
        params.put("IZ", lossTest.getIz());
        params.put("IR",  lossTest.getIr());
        params.put("IX",  lossTest.getIx());
        params.put("EFF",  lossTest.getEff());

        params.put("TESTED_BY", data.getTestedBy().getFullName());
        params.put("TESTED_BY_POS", testedByPosition.getPosition().getName());
        params.put("RECOMMENDING_APPROVAL", data.getRecommendingApprovalUser().getFullName());
        params.put("RECOMMENDING_APPROVAL_POS", recommendingApprovalPosition.getPosition().getName());
        params.put("APPROVED_BY", data.getApprovedBy().getFullName());
        params.put("APPROVED_BY_POS", approvedByPosition.getPosition().getName());

        params.put("REMARKS", data.getRemarks());
        params.put("RECOMMENDATION", data.getRecommendation());

        return params;
    }

    @Override
    @Transactional(readOnly = true)
    public JRDataSource datasourceTransformerTesting(Integer id) {
        List<TransformerVoltageRatioTest> data = transformerVoltageRatioTestRepo.findAllByTransformerTestingId(id);
        return new JRBeanCollectionDataSource(data);
    }

    @Override
    @Transactional("chainedTransactionManager")
    public PostResponse create(TransformerTesting transformerTesting, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();
        MessageFormatter messageFormatter = new MessageFormatter(bindingResult, messageSource, response);
        User user = authenticationFacade.getLoggedIn();

        try {
            TransformerTestingValidator validator = new TransformerTestingValidator();
            validator.validate(transformerTesting, bindingResult);

            if (bindingResult.hasErrors()) {
                messageFormatter.buildErrorMessages();
                return messageFormatter.getResponse();
            }

            if (transformerTesting.getTestedBy() != null && transformerTesting.getTestedBy().getAccountNo() != null) {
                transformerTesting.setTestedBy(userRepo.findOneByAccountNo(transformerTesting.getTestedBy().getAccountNo()));
            } else {
                transformerTesting.setTestedBy(null);
            }

            if (transformerTesting.getRecommendingApprovalUser() != null && transformerTesting.getRecommendingApprovalUser().getAccountNo() != null) {
                transformerTesting.setRecommendingApprovalUser(userRepo.findOneByAccountNo(transformerTesting.getRecommendingApprovalUser().getAccountNo()));
            } else {
                transformerTesting.setRecommendingApprovalUser(null);
            }

            if (transformerTesting.getApprovedBy() != null && transformerTesting.getApprovedBy().getAccountNo() != null) {
                transformerTesting.setApprovedBy(userRepo.findOneByAccountNo(transformerTesting.getApprovedBy().getAccountNo()));
            } else {
                transformerTesting.setApprovedBy(null);
            }

            Transformer transformer = transformerTesting.getTransformer();
            boolean isExisting = Checker.isValidId(transformer.getId());

            com.noreco1.fireflyv2.model.DocumentStatus documentStatus = new com.noreco1.fireflyv2.model.DocumentStatus();
            documentStatus.setId(com.noreco1.fireflyv2.model.enums.DocumentStatus.DOCUMENT_CREATED.getId());
            Workflow wf = new Workflow();
            wf.setId(com.noreco1.fireflyv2.model.enums.Workflow.TRANSFORMER_TESTING.getId());

            if(!isExisting) {
                transformer.setId(null);
                if (transformer.getImpedance() != null) {
                    transformer.setImpedance(
                            transformer.getImpedance()
                                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
                    );
                }
                transformer.setCreatedAt(new Date());
                transformer.setUpdatedAt(new Date());
                transformer.setCreatedBy(user);

                transformer = transformerRepo.save(transformer);

                syncTransformerToMssql(transformer);
            }

            transformerTesting.setTransformer(transformer);
            transformerTesting.setCreatedAt(new Date());
            transformerTesting.setCreatedBy(user);
            transformerTesting.setDocumentStatus(documentStatus);
            transformerTesting.setWorkflow(wf);
            transformerTesting.setTransaction(generatorFacade.transaction());

            TransformerTesting saved = transformerTestingRepo.save(transformerTesting);


//            Test results

            if (!Checker.collectionIsEmpty(transformerTesting.getVoltageRatioTests())) {
                for (TransformerVoltageRatioTest voltageRatioTest : transformerTesting.getVoltageRatioTests()) {
                    voltageRatioTest.setId(null);
                    voltageRatioTest.setTransformerTesting(saved);

                    transformerVoltageRatioTestRepo.save(voltageRatioTest);
                }
            }

            if (!Checker.collectionIsEmpty(transformerTesting.getLossTests())) {
                for (TransformerLossTest transformerLossTest : transformerTesting.getLossTests()) {
                    transformerLossTest.setId(null);
                    transformerLossTest.setTransformerTesting(saved);

                    transformerLossTestRepo.save(transformerLossTest);
                }
            }

            Map<String, Object> newMap = forLogMapMain(saved);

            documentProcessingFacade.processAction(saved.getTransaction(), null, saved.getWorkflow(), user);
            DocumentLog log = documentLoggerFacade.log(saved.getTransaction(), user, null, newMap);

            response.setLogId(log.getId());
            response.setModelId(saved.getId());
            response.setSuccessMessage("Meter testing saved successfully.");

        } catch (Exception e) {
            log.error(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }

        return response;
    }

    @Override
    @Transactional
    public PostResponse process(ProcessDocumentDto postData, BindingResult bindingResult, MessageSource messageSource) {
        PostResponse response = new PostResponse();

        User processedBy = authenticationFacade.getLoggedIn();
        TransformerTesting transformerTesting = transformerTestingRepo.findById(postData.getDocumentId())
                .orElseThrow(() -> new BusinessException("Document not found.", HttpStatus.NOT_FOUND));

        if(transformerTesting.getDocumentStatus().getId() == com.noreco1.fireflyv2.model.enums.DocumentStatus.APPROVED.getId()) {
            throw new BusinessException("Document is already APPROVED.", HttpStatus.CONFLICT);
        }

        Map<String, Object> oldMap = this.forLogMapMain(transformerTesting);

        DocumentWorkflowActionMap actionMap = documentWorkflowActionMapRepo.findById(postData.getWorkflowActionsDto().getActionMapId())
                .orElseThrow(() -> new BusinessException("Workflow action not found.", HttpStatus.NOT_FOUND));

        DocumentStatus afterActionDocumentStatus = actionMap.getAfterActionDocumentStatus();

        if(actionMap.getPropSignatureType() != null) {
            try {
                ClassHelper.setSignatoryValue(transformerTesting, transformerTesting.getClass().getName(), actionMap.getPropSignatureType(), processedBy);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        transformerTesting.setDocumentStatus(afterActionDocumentStatus);
        transformerTesting.setUpdatedAt(new Date());
        transformerTesting = transformerTestingRepo.save(transformerTesting);

        Map<String, Object> newMap = this.forLogMapMain(transformerTesting);
        newMap.put("remarks", postData.getRemarks());

        documentProcessingFacade.processAction(transformerTesting.getTransaction(), actionMap, null, processedBy, DocumentToTableMap.JV.toString());
        documentLoggerFacade.log(transformerTesting.getTransaction(), processedBy, oldMap, newMap);

        response.setSuccessMessage("Transformer testing saved successfully.");
        response.setModelId(transformerTesting.getId());

        return response;

    }

    private void syncTransformerToMssql(Transformer mysqlTransformer) {
        if (mysqlTransformer == null) {
            return;
        }

        com.noreco1.fireflyv2.mssql_model.Transformer mssqlTransformer = mssqlTransformerRepo.findById(mysqlTransformer.getId()).orElse(null);

        if (mssqlTransformer == null) {
            mssqlTransformer = new com.noreco1.fireflyv2.mssql_model.Transformer();
            mssqlTransformer.setId(mysqlTransformer.getId());
            mssqlTransformer.setCreatedAt(mysqlTransformer.getCreatedAt());

            if (mysqlTransformer.getCreatedBy() != null && mysqlTransformer.getCreatedBy().getAccountNo() != null) {
                com.noreco1.fireflyv2.mssql_model.User mssqlUser = mssqlUserRepo.findByAccountNumber(mysqlTransformer.getCreatedBy().getAccountNo());

                mssqlTransformer.setCreatedBy(mssqlUser);
            }
        }

        // copy other fields
        mssqlTransformer.setSerialNo(mysqlTransformer.getSerialNo());
        mssqlTransformer.setOwner(mysqlTransformer.getOwner());
        mssqlTransformer.setOwnerAddress(mysqlTransformer.getOwnerAddress());
        mssqlTransformer.setBrand(mysqlTransformer.getBrand() != null ? mysqlTransformer.getBrand().getId() : null);
        mssqlTransformer.setKva(mysqlTransformer.getKva());
        mssqlTransformer.setPrimaryVoltage(mysqlTransformer.getPrimaryVoltage() != null ? mysqlTransformer.getPrimaryVoltage().getId() : null);
        mssqlTransformer.setSecondaryVoltage(mysqlTransformer.getSecondaryVoltage() != null ? mysqlTransformer.getSecondaryVoltage().getId() : null);
        mssqlTransformer.setImpedance(mysqlTransformer.getImpedance());
        mssqlTransformer.setPolarity(mysqlTransformer.getPolarity());
        mssqlTransformer.setCoreType(mysqlTransformer.getCoreType());
        mssqlTransformer.setBushing(mysqlTransformer.getBushing());
        mssqlTransformer.setType(mysqlTransformer.getType());
        mssqlTransformer.setUpdatedAt(mysqlTransformer.getUpdatedAt());

        mssqlTransformerRepo.save(mssqlTransformer);
    }

    private Map<String, Object> forLogMapMain(TransformerTesting entity) {
        return documentLoggerFacade.makeLog(entity);
    }

}
