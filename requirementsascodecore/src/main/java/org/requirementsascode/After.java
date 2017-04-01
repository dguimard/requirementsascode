package org.requirementsascode;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class After implements Predicate<UseCaseRunner>{
	private Optional<UseCaseStep> previousStep;

	public After(Optional<UseCaseStep> previousStep) {
		this.previousStep = previousStep;
	}
	
	@Override
	public boolean test(UseCaseRunner useCaseRunner) {
		Optional<UseCaseStep> latestStep = useCaseRunner.latestStep();
		boolean isSystemAtRightStep = 
			Objects.equals(previousStep, latestStep);
		return isSystemAtRightStep;	
	}
}
