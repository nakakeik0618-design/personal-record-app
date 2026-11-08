import { Navigate, useNavigate, useParams } from 'react-router'
import { createRecord } from '../api/records'
import { RecordForm, type RecordFormValues } from '../components/RecordForm'
import { today } from '../records/format'
import { findModeBySlug } from '../records/modes'

/** SC-05 記録作成画面 */
export function RecordCreatePage() {
  const { modeSlug } = useParams()
  const navigate = useNavigate()
  const mode = findModeBySlug(modeSlug)

  // URL のモード名が正しくない場合はモード選択画面へ
  if (!mode) {
    return <Navigate to="/records/new" replace />
  }

  const { mode: modeValue } = mode

  async function handleSubmit(values: RecordFormValues) {
    const record = await createRecord({ mode: modeValue, ...values })
    navigate(`/records/${record.id}`, { replace: true })
  }

  return (
    <RecordForm
      key={mode.slug}
      heading={`新しい記録（${mode.label}）`}
      mode={mode}
      initialValues={{ title: '', targetDate: today(), details: {} }}
      cancelTo="/records/new"
      onSubmit={handleSubmit}
    />
  )
}
