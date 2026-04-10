import { useEffect, useState } from "react";
import { DEFAULT_RUN_ID, fetchSimulationResult } from "../services/api";

export function useSimulationResult(runId = DEFAULT_RUN_ID, shouldFetch = true) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!runId || !shouldFetch) {
      setData(null);
      setError(null);
      setIsLoading(false);
      return;
    }

    let active = true;
    setIsLoading(true);
    setError(null);
    setData(null);

    fetchSimulationResult(runId)
      .then((result) => {
        if (active) {
          setData(result);
        }
      })
      .catch((err) => {
        if (active) {
          setError(err);
        }
      })
      .finally(() => {
        if (active) {
          setIsLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [runId, shouldFetch]);

  return { data, error, isLoading };
}
