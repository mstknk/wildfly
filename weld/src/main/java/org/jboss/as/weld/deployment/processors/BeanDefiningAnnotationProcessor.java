package org.jboss.as.weld.deployment.processors;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static org.jboss.as.weld.discovery.AnnotationType.FOR_CLASSINFO;
import static org.jboss.as.weld.util.Indices.ANNOTATION_PREDICATE;
import static org.jboss.as.weld.util.Indices.getAnnotatedClasses;

import java.util.Collection;

import javax.transaction.TransactionScoped;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.weld.CdiAnnotations;
import org.jboss.as.weld.deployment.WeldAttachments;
import org.jboss.as.weld.discovery.AnnotationType;
import org.jboss.jandex.DotName;


/**
 * Determines the set of bean defining annotations as defined by the CDI specification and attaches them under
 * {@link WeldAttachments#BEAN_DEFINING_ANNOTATIONS}.
 *
 * @author Jozef Hartinger
 *
 */
public class BeanDefiningAnnotationProcessor implements DeploymentUnitProcessor {

    private static final DotName VIEW_SCOPED_NAME = DotName.createSimple("javax.faces.view.ViewScoped");
    private static final DotName FLOW_SCOPED_NAME = DotName.createSimple("javax.faces.flow.FlowScoped");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (deploymentUnit.getParent() != null) {
            return; // only run for top-level deployments
        }

        final CompositeIndex index = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);

        addAnnotations(deploymentUnit, CdiAnnotations.BUILT_IN_SCOPES);
        // EE7 built-in scopes
        addAnnotation(deploymentUnit, new AnnotationType(TransactionScoped.class));
        addAnnotation(deploymentUnit, new AnnotationType(VIEW_SCOPED_NAME, true));
        addAnnotation(deploymentUnit, new AnnotationType(FLOW_SCOPED_NAME, true));

        addAnnotations(deploymentUnit, getAnnotationsAnnotatedWith(index, CdiAnnotations.NORM_SCOPE.getDotName()));
        addAnnotations(deploymentUnit, getAnnotationsAnnotatedWith(index, CdiAnnotations.SCOPE));
    }

    private static void addAnnotations(final DeploymentUnit deploymentUnit, Collection<AnnotationType> annotations) {
        for(AnnotationType annotation : annotations){
            addAnnotation(deploymentUnit, annotation);
        }
    }

    private static void addAnnotation(final DeploymentUnit deploymentUnit, AnnotationType annotation) {
        deploymentUnit.addToAttachmentList(WeldAttachments.BEAN_DEFINING_ANNOTATIONS, annotation);
    }

    private Collection<AnnotationType> getAnnotationsAnnotatedWith(CompositeIndex index, DotName annotationName) {
        return transform(filter(getAnnotatedClasses(index.getAnnotations(annotationName)), ANNOTATION_PREDICATE), FOR_CLASSINFO);
    }

    @Override
    public void undeploy(DeploymentUnit context) {
    }
}
