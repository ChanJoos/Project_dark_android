import unittest
from ci_scope_v216 import classify
class ScopeTest(unittest.TestCase):
    def test_reviewed_blob(self):
        self.assertFalse(classify(['app/known.java'], {'app/known.java':'abc'}, {'app/known.java':'abc'})[0])
    def test_changed_reviewed_blob_falls_back(self):
        self.assertTrue(classify(['app/known.java'], {'app/known.java':'new'}, {'app/known.java':'abc'})[0])
    def test_unknown_and_deleted_runtime_fall_back(self):
        self.assertTrue(classify(['app/other.java'], {}, {})[0])
        self.assertTrue(classify(['app/known.java'], {}, {'app/known.java':'abc'})[0])
    def test_documentation_does_not_need_game_regression(self):
        self.assertFalse(classify(['docs/CHAT_HANDOFF.md', 'CHAT_HANDOFF.md', 'PROJECT_STATE.md', 'DECISION_LOG.md', 'DIRECTOR_BACKLOG.md'], {}, {})[0])
    def test_changed_historical_document_hash_does_not_trigger_game_regression(self):
        self.assertFalse(classify(['docs/checkpoint.md'], {'docs/checkpoint.md':'new'}, {'docs/checkpoint.md':'old'})[0])
        self.assertTrue(classify(['docs/checkpoint.md'], {}, {}, True)[0])
    def test_final_integration_forces_full(self):
        self.assertTrue(classify([], {}, {}, True)[0])
if __name__ == '__main__':
    unittest.main()
